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
import top.starwindv.exdeorum.api.ExDeorumApi;

// Exposes a FluidTank through the Fabric Transfer API with transaction support,
// using the same eager-mutation/snapshot-on-abort approach as ItemStackHandlerStorage.
public class FluidTankStorage implements Storage<FluidVariant> {
    // 26.2 quantifies fluids in droplets — one vanilla bucket holds 81000 of them (see Fabric's
    // EmptyBucketStorage) — while Ex Deorum's internal volumes are millibuckets with one bucket
    // = 1000 mB. Every Transfer API boundary speaks droplets and converts here.
    public static final long DROPLETS_PER_BUCKET = 81000L;
    public static final long DROPLETS_PER_MB = DROPLETS_PER_BUCKET / ExDeorumApi.BUCKET_VOLUME;

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

        var stack = new FluidStack(resource.getFluid(), (int) Math.min(maxAmount / DROPLETS_PER_MB, Integer.MAX_VALUE));
        var filled = this.tank.fill(stack, FluidAction.EXECUTE);
        return filled * DROPLETS_PER_MB;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (resource.hasComponents()) {
            return 0;
        }

        participate(transaction);

        var drained = this.tank.drain(new FluidStack(resource.getFluid(), (int) Math.min(maxAmount / DROPLETS_PER_MB, Integer.MAX_VALUE)), FluidAction.EXECUTE);
        return drained.getAmount() * DROPLETS_PER_MB;
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
            return this.isResourceBlank() ? 0 : FluidTankStorage.this.tank.getFluidAmount() * DROPLETS_PER_MB;
        }

        @Override
        public long getCapacity() {
            return FluidTankStorage.this.tank.getCapacity() * DROPLETS_PER_MB;
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
