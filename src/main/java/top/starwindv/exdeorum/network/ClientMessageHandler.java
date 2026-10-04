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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.blockentity.EBlockEntity;
import top.starwindv.exdeorum.menu.AbstractMachineMenu;

public class ClientMessageHandler {
    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(MenuPropertyMessage.TYPE, ClientMessageHandler::handleMenuProperty);
        ClientPlayNetworking.registerGlobalReceiver(VisualUpdateMessage.TYPE, ClientMessageHandler::handleVisualUpdate);
    }

    private static void handleVisualUpdate(VisualUpdateMessage msg, ClientPlayNetworking.Context ctx) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            ClientLevel level = client.level;

            if (level != null && level.getBlockEntity(msg.pos()) instanceof EBlockEntity blockEntity) {
                if (msg.payload() == null) {
                    if (blockEntity != msg.blockEntity() && msg.blockEntity() != null) {
                        blockEntity.copyVisualData(msg.blockEntity());
                    } else {
                        ExDeorum.LOGGER.warn("Failed syncing visual data from server for " + msg.pos().toShortString());
                    }
                } else {
                    blockEntity.readVisualData(msg.payload());
                }
            }
        });
    }

    private static void handleMenuProperty(MenuPropertyMessage msg, ClientPlayNetworking.Context ctx) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            Player player = client.player;

            if (player != null && player.containerMenu instanceof AbstractMachineMenu<?> menu && menu.containerId == msg.containerId()) {
                menu.setClientProperty(msg.index(), msg.value());
            }
        });
    }
}
