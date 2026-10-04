/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.Result;
import top.starwindv.exdeorum.blockentity.helper.FluidTank;
import top.starwindv.exdeorum.fluid.FluidAction;
import top.starwindv.exdeorum.fluid.FluidStack;

// Exposes a FluidTank through the Fabric Transfer API with transaction support,
// using the same eager-mutation/snapshot-on-abort approach as ItemStackHandlerStorage.
public class FluidTankStorage implements Storage<FluidVariant> {
    private final FluidTank tank;
    private final Map<TransactionContext, FluidStack> snapshots = new HashMap<>();

    public FluidTankStorage(FluidTank tank) {
        this.tank = tank;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (resource.hasComponents()) {
            return 0;
        }

        participate(transaction);

        var stack = new FluidStack(resource.getFluid(), (int) Math.min(maxAmount, Integer.MAX_VALUE));
        return this.tank.fill(stack, FluidAction.EXECUTE);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (resource.hasComponents()) {
            return 0;
        }

        participate(transaction);

        var drained = this.tank.drain(new FluidStack(resource.getFluid(), (int) Math.min(maxAmount, Integer.MAX_VALUE)), FluidAction.EXECUTE);
        return drained.getAmount();
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return java.util.List.<StorageView<FluidVariant>>of(new TankView()).iterator();
    }

    // Read-only live view of the tank contents
    private class TankView implements StorageView<FluidVariant> {
        @Override
        public boolean isResourceBlank() {
            return FluidTankStorage.this.tank.isEmpty();
        }

        @Override
        public FluidVariant getResource() {
            return this.isResourceBlank() ? FluidVariant.blank() : FluidVariant.of(FluidTankStorage.this.tank.getFluid().getFluid());
        }

        @Override
        public long getAmount() {
            return this.isResourceBlank() ? 0 : FluidTankStorage.this.tank.getFluidAmount();
        }

        @Override
        public long getCapacity() {
            return FluidTankStorage.this.tank.getCapacity();
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return FluidTankStorage.this.extract(resource, maxAmount, transaction);
        }
    }

    private void participate(TransactionContext transaction) {
        if (this.snapshots.containsKey(transaction)) {
            return;
        }

        this.snapshots.put(transaction, this.tank.getFluid().copy());

        transaction.addCloseCallback((ctx, result) -> {
            var restored = this.snapshots.remove(ctx);

            if (result == Result.ABORTED && restored != null) {
                this.tank.setFluid(restored);
            }
        });
    }
}
