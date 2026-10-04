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

package top.starwindv.exdeorum.blockentity.helper;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// Stand-in for NeoForge's EnergyStorage with the same save format.
public class EnergyHelper {
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public EnergyHelper(int capacity) {
        this(capacity, capacity, capacity);
    }

    public EnergyHelper(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public EnergyHelper(int capacity, int maxReceive, int maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    public int receiveEnergy(int toReceive, boolean simulate) {
        int energyReceived = Math.min(this.capacity - this.energy, Math.min(this.maxReceive, toReceive));

        if (!simulate) {
            this.energy += energyReceived;
        }

        return energyReceived;
    }

    public int extractEnergy(int toExtract, boolean simulate) {
        int energyExtracted = Math.min(this.energy, Math.min(this.maxExtract, toExtract));

        if (!simulate) {
            this.energy -= energyExtracted;
        }

        return energyExtracted;
    }

    public int getEnergyStored() {
        return this.energy;
    }

    public int getMaxEnergyStored() {
        return this.capacity;
    }

    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    public void setStoredEnergy(int energy) {
        this.energy = energy;
    }

    public void deserialize(ValueInput input) {
        this.energy = input.getIntOr("Energy", 0);
    }

    public void serialize(ValueOutput output) {
        output.putInt("Energy", this.energy);
    }
}
