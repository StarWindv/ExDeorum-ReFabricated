/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.blockentity.helper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// Equivalent of NeoForge's ItemStackHandler with the same save format.
public class ItemStackHandler {
    protected List<ItemStack> stacks = new ArrayList<>();

    // NeoForge's ItemStackHandler() defaults to a single slot
    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        for (var i = 0; i < size; ++i) {
            this.stacks.add(ItemStack.EMPTY);
        }
    }

    public void setSize(int size) {
        this.stacks = new ArrayList<>(size);

        for (var i = 0; i < size; ++i) {
            this.stacks.add(ItemStack.EMPTY);
        }
    }

    public int getSlots() {
        return this.stacks.size();
    }

    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);

        var existing = this.stacks.get(slot);
        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }
        validateSlotIndex(slot);

        var existing = this.stacks.get(slot);

        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                onContentsChanged(slot);
            }
            return existing.copyWithCount(toExtract);
        }
    }

    public int getSlotLimit(int slot) {
        return 99;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public void serialize(ValueOutput output) {
        var itemList = output.list("Items", ItemStackWithSlot.CODEC);

        for (var i = 0; i < this.stacks.size(); i++) {
            var stack = this.stacks.get(i);

            if (!stack.isEmpty()) {
                itemList.add(new ItemStackWithSlot(i, stack));
            }
        }

        output.putInt("Size", this.stacks.size());
    }

    public void deserialize(ValueInput input) {
        this.setSize(input.getIntOr("Size", this.stacks.size()));
        input.listOrEmpty("Items", ItemStackWithSlot.CODEC).forEach(slot -> {
            if (slot.isValidInContainer(this.stacks.size())) {
                this.stacks.set(slot.slot(), slot.stack());
            }
        });
        onLoad();
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= this.stacks.size()) {
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.stacks.size() + ")");
        }
    }

    protected void onLoad() {
    }

    protected void onContentsChanged(int slot) {
    }
}
