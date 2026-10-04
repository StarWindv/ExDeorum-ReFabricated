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

package top.starwindv.exdeorum.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import top.starwindv.exdeorum.block.InfestedLeavesBlock;
import top.starwindv.exdeorum.blockentity.InfestedLeavesBlockEntity;
import top.starwindv.exdeorum.registry.EBlocks;
import top.starwindv.exdeorum.registry.ESounds;

public class SilkwormItem extends BlockTransformingItem {
    public SilkwormItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canTransformState(BlockState state) {
        return !state.isAir() && state.is(BlockTags.LEAVES) && state.getBlock() != EBlocks.INFESTED_LEAVES.get();
    }

    @Override
    protected void doTransformState(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            // Replace with infested block
            InfestedLeavesBlock.setBlock(level, pos, state);

            level.playSound(null, pos, ESounds.SILK_WORM_INFEST.get(), SoundSource.BLOCKS);

            // Set mimic
            if (level.getBlockEntity(pos) instanceof InfestedLeavesBlockEntity leaves) {
                leaves.setMimic(state);
            }
        }
    }

    // The "gross noise when you discover a silk worm" is played from ItemEntityMixin, which
    // replaces NeoForge's IItemExtension#onEntityItemUpdate hook.
}
