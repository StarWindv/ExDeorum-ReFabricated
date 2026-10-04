/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.util;

import java.util.ArrayList;

import top.starwindv.exdeorum.blockentity.EBlockEntity;

// NeoForge calls BlockEntity#onLoad once the block entity has a level. Fabric has no such
// hook, and the closest vanilla equivalent, BlockEntity#setLevel, runs while the block entity
// is still being registered: the chunk is mid-load and the block entity has not been handed to
// the level ticker. Work done from there (reading neighbouring blocks, resolving recipes,
// re-entering the light engine) executes inside the chunk system, and with C2ME's async
// chunk system that deadlocks the world load.
//
// Instead the block entities are collected here and drained from a tick, which vanilla only
// runs for block entities in chunks that finished loading.
public final class PendingBlockEntityLoads {
    private static final ArrayList<EBlockEntity> PENDING = new ArrayList<>();

    private PendingBlockEntityLoads() {
    }

    // Called from BlockEntity#setLevel, which can run on any thread, including inside the
    // chunk system. Only the list itself is guarded; the work is done later on a tick.
    public static synchronized void add(EBlockEntity blockEntity) {
        PENDING.add(blockEntity);
    }

    // Must run on the server or client thread.
    public static void flush() {
        EBlockEntity[] queued;

        synchronized (PendingBlockEntityLoads.class) {
            if (PENDING.isEmpty()) {
                return;
            }

            queued = PENDING.toArray(new EBlockEntity[0]);
            PENDING.clear();
        }

        for (var blockEntity : queued) {
            // A block entity can be removed again before its first tick, in which case
            // loading it would touch a level that is no longer there.
            if (blockEntity.isRemoved() || blockEntity.getLevel() == null) {
                continue;
            }

            blockEntity.onLoad();
        }
    }

    public static synchronized int size() {
        return PENDING.size();
    }
}