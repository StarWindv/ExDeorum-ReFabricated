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

package top.starwindv.exdeorum.fluid;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import top.starwindv.exdeorum.registry.EBlocks;
import top.starwindv.exdeorum.registry.EFluids;
import top.starwindv.exdeorum.registry.EItems;
import top.starwindv.exdeorum.ExDeorum;

// A purple water-like fluid that applies witchy effects to entities (see WitchWaterBlock).
// Fluid type behaviours that were FluidType properties on NeoForge (no fall damage,
// extinguishing fire, supporting boating) are implemented with mixins.
public abstract class WitchWaterFluid extends FlowingFluid {
    // Fluid rendering textures, used by the client fluid model registration
    public static final Identifier STILL_TEXTURE = ExDeorum.loc("block/witch_water_still");
    public static final Identifier FLOWING_TEXTURE = ExDeorum.loc("block/witch_water_flowing");
    public static final Identifier OVERLAY_TEXTURE = Identifier.withDefaultNamespace("block/water_overlay");

    @Override
    public abstract Fluid getFlowing();

    @Override
    public abstract Fluid getSource();

    @Override
    public Item getBucket() {
        return EItems.WITCH_WATER_BUCKET.get();
    }

    @Override
    public int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return EBlocks.WITCH_WATER.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid other) {
        return other == getSource() || other == getFlowing();
    }

    @Override
    public int getDropOff(LevelReader level) {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 5;
    }

    @Override
    public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.is(FluidTags.WATER) && !fluid.isSame(getSource());
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0f;
    }

    @Override
    protected boolean canConvertToSource(net.minecraft.server.level.ServerLevel level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        // nothing drops when this fluid destroys a block, same as vanilla water in a void world
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    public static class Source extends WitchWaterFluid {
        @Override
        public Fluid getFlowing() {
            return EFluids.WITCH_WATER_FLOWING.get();
        }

        @Override
        public Fluid getSource() {
            return EFluids.WITCH_WATER.get();
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }
    }

    public static class Flowing extends WitchWaterFluid {
        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public Fluid getFlowing() {
            return EFluids.WITCH_WATER_FLOWING.get();
        }

        @Override
        public Fluid getSource() {
            return EFluids.WITCH_WATER.get();
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }
    }
}
