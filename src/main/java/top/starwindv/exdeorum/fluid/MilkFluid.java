/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.fluid;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import top.starwindv.exdeorum.registry.EBlocks;
import top.starwindv.exdeorum.registry.EFluids;

// Registered only for recipe parity with the NeoForge version, which ships a milk fluid.
// Milk never exists in world generation and has no bucket item; the porcelain milk
// bucket converts to this fluid for barrel fluid mixing recipes.
public abstract class MilkFluid extends FlowingFluid {
    @Override
    public abstract Fluid getFlowing();

    @Override
    public abstract Fluid getSource();

    @Override
    public Item getBucket() {
        return null;
    }

    @Override
    public int getSlopeFindDistance(LevelReader level) {
        return 4;
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return EBlocks.MILK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
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
        return false;
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
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.COW_MILK);
    }

    public static class Source extends MilkFluid {
        @Override
        public Fluid getFlowing() {
            return EFluids.MILK_FLOWING.get();
        }

        @Override
        public Fluid getSource() {
            return EFluids.MILK.get();
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

    public static class Flowing extends MilkFluid {
        @Override
        protected void createFluidStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public Fluid getFlowing() {
            return EFluids.MILK_FLOWING.get();
        }

        @Override
        public Fluid getSource() {
            return EFluids.MILK.get();
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
