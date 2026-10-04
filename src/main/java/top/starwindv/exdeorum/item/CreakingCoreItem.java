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

package top.starwindv.exdeorum.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CreakingCoreItem extends BlockTransformingItem {
    public CreakingCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canTransformState(BlockState state) {
        return state.is(Blocks.PALE_OAK_LOG);
    }

    @Override
    protected void doTransformState(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        var newState = Blocks.CREAKING_HEART.defaultBlockState()
                .setValue(CreakingHeartBlock.AXIS, state.getValue(BlockStateProperties.AXIS))
                .setValue(CreakingHeartBlock.NATURAL, false);
        level.setBlock(pos, newState, 3);
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(Blocks.CREAKING_HEART.defaultBlockState()));
    }
}
