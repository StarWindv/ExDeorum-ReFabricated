/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.Result;
import net.minecraft.world.item.ItemStack;
import top.starwindv.exdeorum.blockentity.helper.ItemStackHandler;

// Exposes an ItemStackHandler through the Fabric Transfer API with transaction support.
// Mutations are applied eagerly so later operations in the same transaction observe them;
// a snapshot taken when a transaction first participates is restored if it aborts.
public class ItemStackHandlerStorage implements Storage<ItemVariant> {
    private final ItemStackHandler handler;
    private final Map<TransactionContext, ItemStack[]> snapshots = new HashMap<>();

    public ItemStackHandlerStorage(ItemStackHandler handler) {
        this.handler = handler;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        participate(transaction);

        long inserted = 0;
        var remaining = resource.toStack((int) Math.min(maxAmount, Integer.MAX_VALUE));

        for (var slot = 0; slot < this.handler.getSlots() && !remaining.isEmpty(); ++slot) {
            var remainder = this.handler.insertItem(slot, remaining.copy(), false);
            inserted += remaining.getCount() - remainder.getCount();
            remaining = remainder;
        }

        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        participate(transaction);

        long extracted = 0;

        for (var slot = 0; slot < this.handler.getSlots() && extracted < maxAmount; ++slot) {
            var contained = this.handler.getStackInSlot(slot);

            if (!contained.isEmpty() && resource.matches(contained)) {
                var toExtract = (int) Math.min(maxAmount - extracted, contained.getCount());
                var out = this.handler.extractItem(slot, toExtract, false);
                extracted += out.getCount();
            }
        }

        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new Iterator<>() {
            private int slot = 0;

            @Override
            public boolean hasNext() {
                return this.slot < ItemStackHandlerStorage.this.handler.getSlots();
            }

            @Override
            public StorageView<ItemVariant> next() {
                return new SlotView(this.slot++);
            }
        };
    }

    // A view of one slot which follows the handler's contents
    private class SlotView implements StorageView<ItemVariant> {
        private final int slot;

        SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            var contained = ItemStackHandlerStorage.this.handler.getStackInSlot(this.slot);

            if (contained.isEmpty() || !resource.matches(contained)) {
                return 0;
            }

            participate(transaction);

            var toExtract = (int) Math.min(maxAmount, contained.getCount());
            return ItemStackHandlerStorage.this.handler.extractItem(this.slot, toExtract, false).getCount();
        }

        @Override
        public boolean isResourceBlank() {
            return ItemStackHandlerStorage.this.handler.getStackInSlot(this.slot).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(ItemStackHandlerStorage.this.handler.getStackInSlot(this.slot));
        }

        @Override
        public long getAmount() {
            return ItemStackHandlerStorage.this.handler.getStackInSlot(this.slot).getCount();
        }

        @Override
        public long getCapacity() {
            return ItemStackHandlerStorage.this.handler.getSlotLimit(this.slot);
        }
    }

    private void participate(TransactionContext transaction) {
        if (this.snapshots.containsKey(transaction)) {
            return;
        }

        var snapshot = new ItemStack[this.handler.getSlots()];

        for (var i = 0; i < snapshot.length; ++i) {
            snapshot[i] = this.handler.getStackInSlot(i);
        }

        this.snapshots.put(transaction, snapshot);

        transaction.addCloseCallback((ctx, result) -> {
            var restored = this.snapshots.remove(ctx);

            if (result == Result.ABORTED && restored != null) {
                for (var i = 0; i < restored.length; ++i) {
                    this.handler.setStackInSlot(i, restored[i]);
                }
            }
        });
    }
}
