/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

// Simple live StorageView over a single fluid variant, shared by the Ex Deorum storages.
public class TankAccess implements StorageView<FluidVariant> {
    private final FluidVariant variant;
    private final long amount;
    private final long capacity;

    public TankAccess(FluidVariant variant, long amount, long capacity) {
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
        return 0;
    }
}
