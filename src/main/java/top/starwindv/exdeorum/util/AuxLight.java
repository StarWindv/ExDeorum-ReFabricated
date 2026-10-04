/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.util;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

// Dynamic block light for blocks whose light level depends on their contents, such as a
// crucible holding lava. This replaces NeoForge's AuxLightManager: values are stored per
// level and read back by BlockLightEngineMixin when the light engine asks how much light
// a position emits.
//
// getLightAt is called for every block of every light update on every lighting worker thread,
// so the read path has to stay lock free. A ConcurrentHashMap keeps that true, where a
// Long2IntOpenHashMap behind a monitor serialised every worker thread in the game.
//
// The write path never touches the light engine directly. Block entities load while their
// chunk is still being registered, and calling LightEngine#checkBlock from there re-enters
// the light engine mid-load, which deadlocks the chunk system. Relight requests are queued
// and drained from a tick instead, once the chunk is fully loaded.
public final class AuxLight {
    private static final Map<Level, ConcurrentHashMap<Long, Integer>> LIGHT_BY_LEVEL = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<RelightRequest> PENDING_RELIGHTS = new ConcurrentLinkedQueue<>();

    private record RelightRequest(Level level, BlockPos pos) {
    }

    // Sets the light emitted at a position, or clears it when light <= 0,
    // and asks the light engine to re-evaluate that position.
    public static void setLightAt(Level level, BlockPos pos, int light) {
        // A block entity being removed can still be detached from its level, and
        // ConcurrentHashMap rejects a null key.
        if (level == null) {
            return;
        }

        var key = pos.asLong();
        var map = LIGHT_BY_LEVEL.computeIfAbsent(level, _ -> new ConcurrentHashMap<>());
        Integer previous;
        Integer boxed = light > 0 ? light : null;

        if (light > 0) {
            previous = map.put(key, light);
        } else {
            previous = map.remove(key);
        }

        if (!Objects.equals(previous, boxed)) {
            PENDING_RELIGHTS.add(new RelightRequest(level, pos.immutable()));
        }
    }

    public static int getLightAt(Level level, long packedPos) {
        var map = LIGHT_BY_LEVEL.get(level);

        if (map == null || map.isEmpty()) {
            return 0;
        }

        var light = map.get(packedPos);

        return light == null ? 0 : light;
    }

    // Applies the relight requests queued while block entities were loading. Must run on the
    // server (or client) thread at a point where the affected chunks are fully loaded.
    public static void flushRelights() {
        RelightRequest request;

        while ((request = PENDING_RELIGHTS.poll()) != null) {
            relight(request.level(), request.pos());
        }
    }

    // Called when a level is unloaded so stale levels do not pile up
    public static void clear(Level level) {
        LIGHT_BY_LEVEL.remove(level);
        PENDING_RELIGHTS.removeIf(request -> request.level() == level);
    }

    private static void relight(Level level, BlockPos pos) {
        // The level may have been torn down between the request being queued and drained.
        if (!level.hasChunkAt(pos)) {
            return;
        }

        var lightEngine = level.getChunkSource().getLightEngine();

        lightEngine.checkBlock(pos);

        // Light has to be re-propagated into the neighbouring blocks as well
        for (var direction : Direction.values()) {
            lightEngine.checkBlock(pos.relative(direction));
        }
    }
}