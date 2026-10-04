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

package top.starwindv.exdeorum.compat.top;

import mcjty.theoneprobe.api.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.blockentity.AbstractCrucibleBlockEntity;
import top.starwindv.exdeorum.blockentity.BarrelBlockEntity;
import top.starwindv.exdeorum.blockentity.InfestedLeavesBlockEntity;
import top.starwindv.exdeorum.blockentity.SieveBlockEntity;
import top.starwindv.exdeorum.compat.CompatUtil;
import top.starwindv.exdeorum.util.TranslationKeys;
import top.starwindv.exdeorum.registry.EBlocks;

public class ExDeorumInfoProvider implements IProbeInfoProvider {
    @Override
    public Identifier getID() {
        return Identifier.fromNamespaceAndPath(ExDeorum.ID, "info_provider");
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player playerEntity, Level level, BlockState state, IProbeHitData data) {
        var te = level.getBlockEntity(data.getPos());

        if (state.getBlock() == EBlocks.INFESTED_LEAVES.get()) {
            if (te instanceof InfestedLeavesBlockEntity leaves) {
                int progress = 100 * leaves.getProgress() / InfestedLeavesBlockEntity.MAX_PROGRESS;

                info.text(CompoundText.create().style(TextStyleClass.LABEL).text("Progress: ").style(TextStyleClass.WARNING).text(progress + "%"));
            }
        } else if (te instanceof BarrelBlockEntity barrel) {
            short volume = barrel.compost;

            if (volume == 1000 || barrel.isBrewing()) {
                int progress = (int) (barrel.progress * 100.0f);

                if (progress == 100) {
                    info.text(Component.translatable(TranslationKeys.INFESTED_LEAVES_FULLY_INFESTED).withStyle(ChatFormatting.GRAY));
                } else {
                    info.text(CompoundText.create().style(TextStyleClass.LABEL).text("Progress: ").style(TextStyleClass.WARNING).text(progress + "%"));
                }
            } else if (volume > 0) {
                int volumePercent = (int) (volume / 10.0f);

                info.text(CompoundText.create().style(TextStyleClass.LABEL).text("Volume: ").style(TextStyleClass.WARNING).text(volumePercent + "%"));
            } else if (barrel.isBurning()) {
                int progress = 300 - (int) (barrel.progress * 300.0f);

                info.text(CompoundText.create().style(TextStyleClass.ERROR).text("Burning! ").style(TextStyleClass.WARNING).text(progress / 20 + "s"));
            }
            if (playerEntity.isShiftKeyDown()) {
                info.tank(barrel.getTank());
            }
        } else if (te instanceof AbstractCrucibleBlockEntity crucible) {
            info.text(CompoundText.create().style(TextStyleClass.LABEL).text("Rate: ").style(TextStyleClass.WARNING).text(crucible.getMeltingRate() + "x"));
            if (playerEntity.isShiftKeyDown()) {
                info.tank(crucible.getTank());
            }
        } else if (te instanceof SieveBlockEntity sieve) {
            var logic = sieve.getLogic();
            if (!logic.getContents().isEmpty()) {
                info.text(CompoundText.create().style(TextStyleClass.LABEL).text("Progress: ").style(TextStyleClass.WARNING).text((Math.round(1000 * logic.getProgress()) / 10) + "%"));
            }
            if (playerEntity.isShiftKeyDown()) {
                var mesh = logic.getMesh();
                info.horizontal(info.defaultLayoutStyle().spacing(10).alignment(ElementAlignment.ALIGN_CENTER))
                        .item(mesh, info.defaultItemStyle().width(16).height(16))
                        .text(CompoundText.create().info(mesh.getDescriptionId()));
                if (mesh.isEnchanted()) {
                    var style = info.defaultTextStyle().height(10);

                    CompatUtil.addEnchantmentsTooltip(mesh, level, component -> {
                        info.text(component, style);
                    });
                }
            }
        }
    }
}
