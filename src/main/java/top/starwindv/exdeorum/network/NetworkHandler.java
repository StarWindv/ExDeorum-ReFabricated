/*
 * Ex Deorum
 * Copyright (c) 2024 thedarkcolour
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Modifications Copyleft (c) 2026 StarWindv
 * Ported to Fabric
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class NetworkHandler {
    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(MenuPropertyMessage.TYPE, MenuPropertyMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(VisualUpdateMessage.TYPE, VisualUpdateMessage.STREAM_CODEC);
    }

    public static void sendMenuProperty(ServerPlayer player, int containerId, int index, int prevSieveEnergy) {
        ServerPlayNetworking.send(player, new MenuPropertyMessage(containerId, index, prevSieveEnergy));
    }

    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos pos, VisualUpdateMessage message) {
        for (var player : PlayerLookup.tracking(level, pos)) {
            ServerPlayNetworking.send(player, message);
        }
    }
}
