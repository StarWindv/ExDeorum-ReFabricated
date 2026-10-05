/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

// Simple live StorageView over a single fluid variant, shared by the Ex Deorum storages.
// Extraction has to go back through the owning storage, because the Transfer API drives
// item to block transfers off the views rather than off the storage itself.
public class TankAccess implements StorageView<FluidVariant> {
    private final Storage<FluidVariant> source;
    private final FluidVariant variant;
    private final long amount;
    private final long capacity;

    public TankAccess(Storage<FluidVariant> source, FluidVariant variant, long amount, long capacity) {
        this.source = source;
        this.variant = variant;
        this.amount = amount;
        this.capacity = capacity;
    }

    @Override
    public boolean isResourceBlank() {
        return this.amount == 0 || this.variant.isBlank();
    }

    @Override
    public FluidVariant getResource() {
        return this.variant;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return this.source.extract(resource, maxAmount, transaction);
    }
}
