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

package top.starwindv.exdeorum.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import top.starwindv.exdeorum.blockentity.helper.EnergyHelper;
import top.starwindv.exdeorum.blockentity.helper.ItemHelper;
import top.starwindv.exdeorum.client.screen.RedstoneControlWidget;
import top.starwindv.exdeorum.menu.MachineData;
import top.starwindv.exdeorum.transfer.RebornEnergyInterop;

import java.util.function.Function;

public abstract class AbstractMachineBlockEntity<M extends AbstractMachineBlockEntity<M>> extends EBlockEntity implements ExtendedMenuProvider<MachineData> {
    public final ItemHelper inventory;
    public final EnergyHelper energy;
    protected int redstoneMode;
    // not saved to NBT
    protected boolean hasRedstonePower;
    // The TeamReborn energy adapter, created lazily by RebornEnergyInterop once some mod
    // provides the API. Kept as Object so this class never references the API directly.
    @Nullable
    private Object energyApi;

    @SuppressWarnings("unchecked")
    public AbstractMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Function<M, ItemHelper> inventory, int maxEnergy) {
        super(type, pos, state);

        this.inventory = inventory.apply((M) this);
        this.energy = new EnergyHelper(maxEnergy);
    }

    /**
     * This machine's energy as seen through the TeamReborn energy API. One adapter per
     * machine, so every cable transaction stages against the same transaction participant.
     */
    public Object getOrCreateEnergyApi() {
        if (this.energyApi == null) {
            this.energyApi = RebornEnergyInterop.createAdapter(this.energy);
        }

        return this.energyApi;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        this.inventory.serialize(output.child("inventory"));
        output.putInt("energy", this.energy.getEnergyStored());
        output.putInt("redstoneMode", this.redstoneMode);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.inventory.deserialize(input.childOrEmpty("inventory"));
        this.energy.setStoredEnergy(input.getIntOr("energy", 0));
        this.redstoneMode = Mth.clamp(input.getIntOr("redstoneMode", 0), 0, 2);
    }

    @Override
    public void onLoad() {
        checkPoweredState(this.level, this.worldPosition);
    }

    public void checkPoweredState(Level level, BlockPos pos) {
        this.hasRedstonePower = level.hasNeighborSignal(pos);
    }

    public void setRedstoneMode(int redstoneMode) {
        this.redstoneMode = redstoneMode;
    }

    public int getRedstoneMode() {
        return this.redstoneMode;
    }

    @Override
    public InteractionResult useWithoutItem(Level level, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public MachineData getScreenOpeningData(ServerPlayer player) {
        return new MachineData(getBlockPos(), (byte) this.redstoneMode);
    }

    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
        }
    }
    protected abstract boolean isRunning();

    protected abstract void tryStartRunning();

    // Only called serverside
    protected abstract void runMachineTick();

    protected abstract int getEnergyConsumption();

    protected void noEnergyTick() {}

    public ItemHelper getItemHandler() {
        return this.inventory;
    }

    public EnergyHelper getEnergyStorage() {
        return this.energy;
    }

    // Used by both sieve and hammer
    public static class ServerTicker<M extends AbstractMachineBlockEntity<M>> implements BlockEntityTicker<M> {
        @Override
        public void tick(Level level, BlockPos pos, BlockState state, M machine) {
            if (machine.redstoneMode == RedstoneControlWidget.REDSTONE_MODE_IGNORED || ((machine.redstoneMode == RedstoneControlWidget.REDSTONE_MODE_UNPOWERED)) != machine.hasRedstonePower) {
                var energyConsumption = machine.getEnergyConsumption();

                if (machine.energy.getEnergyStored() >= energyConsumption) {
                    if (!machine.isRunning()) {
                        machine.tryStartRunning();
                    }
                    if (machine.isRunning()) {
                        machine.energy.extractEnergy(energyConsumption, false);
                        machine.runMachineTick();
                    }
                } else {
                    machine.noEnergyTick();
                }
            }
        }
    }
}
