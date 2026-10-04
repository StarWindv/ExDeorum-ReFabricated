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

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Has same behavior as ItemStackHandler but is more customizable.
public class ItemHelper extends ItemStackHandler implements Container {
    public ItemHelper(int size) {
        super(size);
    }

    // Whether an item can be extracted from this slot (GUI ignores this and just takes it out)
    public boolean canMachineExtract(int slot) {
        return true;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (canMachineExtract(slot)) {
            return takeOutItem(slot, amount, simulate);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack takeOutItem(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }

    public ItemHelper.Slot createSlot(int index, int x, int y) {
        return new ItemHelper.Slot(index, x, y);
    }

    // Container implementation so vanilla Slots can be used in menus

    @Override
    public int getContainerSize() {
        return getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (var i = 0; i < getSlots(); ++i) {
            if (!getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var stack = getStackInSlot(slot);
        setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        setStackInSlot(slot, stack);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (var i = 0; i < getSlots(); ++i) {
            setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public class Slot extends net.minecraft.world.inventory.Slot {
        @Nullable
        private Identifier background;

        public Slot(int index, int x, int y) {
            super(ItemHelper.this, index, x, y);
        }

        public Slot setBackground(Identifier background) {
            this.background = background;
            return this;
        }

        // Vanilla renders this icon in place of an empty slot
        @Override
        @Nullable
        public Identifier getNoItemIcon() {
            return this.background;
        }

        @Override
        public ItemStack remove(int amount) {
            return ItemHelper.this.takeOutItem(getContainerSlot(), amount, false);
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return !ItemHelper.this.takeOutItem(getContainerSlot(), 1, true).isEmpty();
        }
    }
}
