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

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.fabricmc.fabric.api.entity.FakePlayer;
import top.starwindv.exdeorum.fluid.FluidStack;
import top.starwindv.exdeorum.blockentity.BarrelBlockEntity;
import top.starwindv.exdeorum.fluid.FluidContent;
import top.starwindv.exdeorum.util.TranslationKeys;
import top.starwindv.exdeorum.registry.EDataComponents;
import top.starwindv.exdeorum.registry.ESounds;
import top.starwindv.exdeorum.tag.EBlockTags;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WateringCanItem extends Item {
    private static final int WATERING_INTERVAL = 4;
    private static final int STARTUP_TIME = 10;
    private final int capacity;
    private final boolean renewing;
    private final boolean usableInMachines;

    public WateringCanItem(int capacity, Properties properties) {
        super(properties);

        this.capacity = capacity;
        this.renewing = capacity >= 4000;
        this.usableInMachines = false;
    }

    protected WateringCanItem(boolean usableInMachines, Properties properties) {
        super(properties);

        this.capacity = 4000;
        this.renewing = true;
        this.usableInMachines = usableInMachines;
    }

    public static ItemStack getFull(Supplier<? extends Item> wateringCan) {
        var stack = new ItemStack(wateringCan.get());

        if (wateringCan.get() instanceof WateringCanItem can) {
            can.fill(stack, Integer.MAX_VALUE);
        }

        return stack;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public FluidContent getContents(ItemStack stack) {
        return stack.getOrDefault(EDataComponents.WATERING_CAN.get(), FluidContent.EMPTY);
    }

    private static void setContents(ItemStack stack, FluidContent contents) {
        if (contents.isEmpty()) {
            stack.remove(EDataComponents.WATERING_CAN.get());
        } else {
            stack.set(EDataComponents.WATERING_CAN.get(), contents);
        }
    }

    // Fills this can with water up to its capacity, returning the amount accepted
    private int fill(ItemStack stack, int amount) {
        var contents = getContents(stack);
        int space = this.capacity - contents.amount();

        if (space <= 0 || amount <= 0) {
            return 0;
        }

        int filled = Math.min(space, amount);
        setContents(stack, new FluidContent(Fluids.WATER, contents.amount() + filled));
        return filled;
    }

    private static void drainOne(ItemStack stack) {
        var contents = stack.getOrDefault(EDataComponents.WATERING_CAN.get(), FluidContent.EMPTY);

        if (contents.isEmpty()) {
            return;
        }

        setContents(stack, new FluidContent(contents.fluid(), contents.amount() - 1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (this.renewing) {
            return getContents(stack).amount() < this.capacity;
        } else {
            return true;
        }
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return 0x3F76E4;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round((float) getContents(stack).amount() * 13f / (float) this.capacity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        // use the block name which is guaranteed to have a vanilla translation
        tooltip.accept(Component.translatable("block.minecraft.water").append(Component.translatable(TranslationKeys.FRACTION_DISPLAY, getContents(stack).amount(), this.capacity)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var itemInHand = player.getItemInHand(hand);
        var contents = getContents(itemInHand);
        {
            if (contents.amount() < this.capacity) {
                var hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    var pos = hitResult.getBlockPos();
                    var state = level.getBlockState(pos);

                    if (state.getFluidState().getType() == Fluids.WATER && state.getBlock() instanceof BucketPickup pickup) {
                        if (!level.isClientSide()) {
                            fill(itemInHand, 1000);
                            pickup.pickupBlock(player, level, pos, state);
                            pickup.getPickupSound().ifPresent(sound -> player.playSound(sound, 1.0F, 1.0F));
                        }

                        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
                    }
                }
            }

            if (!contents.isEmpty()) {
                var realPlayer = !(player instanceof FakePlayer);

                if (realPlayer) {
                    player.startUsingItem(hand);
                } else if (this.usableInMachines) {
                    onUseTick(level, player, itemInHand, 72000);
                }

                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingTicks) {
        // Watering starts once the startup animation has played and then lines up with
        // WATERING_INTERVAL, so the first watering lands on tick 12. A tap shorter than that
        // does nothing, which has always been the case.
        var useTicks = 72000 - remainingTicks;

        if (useTicks >= STARTUP_TIME || living instanceof FakePlayer) {
            if (!getContents(stack).isEmpty()) {
                // do watering can
                var reachDist = living.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
                var hit = living.pick(reachDist, 0, true);

                if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
                    var pos = blockHit.getBlockPos();
                    var state = level.getBlockState(pos);

                    if (!level.isClientSide()) {
                        if (useTicks % WATERING_INTERVAL == 0) {
                            tryWatering((ServerLevel) level, pos, state);

                            if (!this.renewing || getContents(stack).amount() != this.capacity) {
                                if (!(living instanceof Player player && player.getAbilities().instabuild)) {
                                    drainOne(stack);
                                }
                            }
                        }
                        if (useTicks % 2 == 0) {
                            waterParticles(level, pos, state);
                        }
                        if ((useTicks - STARTUP_TIME) % 20 == 0) {
                            level.playSound(null, pos, ESounds.WATERING_CAN_USE.get(), living.getSoundSource(), this.getClass() == WideWateringCanItem.class ? 0.6f : 0.3f, 1.5f);
                        }
                    }
                }
            } else {
                living.stopUsingItem();
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeCharged) {
        if (timeCharged > STARTUP_TIME) {
            level.playLocalSound(living.getX(), living.getY(), living.getZ(), ESounds.WATERING_CAN_STOP.get(), living.getSoundSource(), 0.6f, 0.7f, false);
        }
        return false;
    }

    protected void tryWatering(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.is(EBlockTags.WATERING_CAN_TICKABLE)) {
            if (state.is(BlockItemTags.SAPLINGS.block())) {
                if (level.getRandom().nextInt(3) == 0) {
                    state.randomTick(level, pos, level.getRandom());
                    level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
                }
            } else if (state.getBlock() instanceof SugarCaneBlock block) {
                var cursor = pos.mutable();
                while (level.isInWorldBounds(cursor.move(0, 1, 0)) && level.getBlockState(cursor).getBlock() == block) {
                    // just keep looping, cursor is moved up each check
                }
                // randomTick only works on the top sugarcane block
                var topState = level.getBlockState(cursor.move(0, -1, 0));
                topState.randomTick(level, cursor, level.getRandom());
            } else {
                state.randomTick(level, pos, level.getRandom());
            }
        } else {
            if (BarrelBlockEntity.isHotFluid(state.getFluidState().getType())) {
                level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
            } else if (state.getBlock() instanceof FarmlandBlock) {
                hydrateFarmland(level, pos, state);
            }
        }
        var below = pos.below();
        var belowState = level.getBlockState(below);
        if (belowState.getBlock() == Blocks.FARMLAND) {
            hydrateFarmland(level, below, belowState);
        }
    }

    private static void hydrateFarmland(ServerLevel level, BlockPos pos, BlockState state) {
        var randomPos = pos.offset(level.getRandom().nextIntBetweenInclusive(-1, 1), 0, level.getRandom().nextIntBetweenInclusive(-1, 1));

        if (randomPos != pos) {
            pos = randomPos;
            state = level.getBlockState(pos);

            if (state.getBlock() != Blocks.FARMLAND) {
                return;
            }
        }

        if (state.getValue(FarmlandBlock.MOISTURE) < 7) {
            level.setBlockAndUpdate(pos, state.setValue(FarmlandBlock.MOISTURE, 7));
        }
    }

    protected void waterParticles(Level level, BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            double x = pos.getX() + 0.5 + level.getRandom().nextGaussian() / 8f;
            double y = pos.getY();
            double z = pos.getZ() + 0.5 + level.getRandom().nextGaussian() / 8f;
            var collisionShape = state.getCollisionShape(level, pos);
            if (!collisionShape.isEmpty()) {
                y += collisionShape.max(Direction.Axis.Y);
            }
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (level.getRandom().nextBoolean()) {
                        serverLevel.sendParticles(ParticleTypes.RAIN, x + i * 0.33, y, z + j * 0.33, 2, 0, 0, 0, 0.2);
                    }
                }
            }
        }
    }
}
