/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import top.starwindv.exdeorum.registry.EFluids;
import top.starwindv.exdeorum.registry.EItems;

// Fluid storage for the porcelain buckets. Like vanilla buckets, the fluid is
// determined by which bucket item the container currently is.
public class PorcelainBucketStorage implements Storage<FluidVariant> {
    private final ContainerItemContext context;

    public PorcelainBucketStorage(ContainerItemContext context) {
        this.context = context;
    }

    private Fluid currentFluid() {
        var item = this.context.getItemVariant().getItem();

        if (item == EItems.PORCELAIN_WATER_BUCKET.get()) {
            return Fluids.WATER;
        } else if (item == EItems.PORCELAIN_LAVA_BUCKET.get()) {
            return Fluids.LAVA;
        } else if (item == EItems.PORCELAIN_WITCH_WATER_BUCKET.get()) {
            return EFluids.WITCH_WATER.get();
        } else if (item == EItems.PORCELAIN_MILK_BUCKET.get()) {
            return EFluids.MILK.get();
        }

        return Fluids.EMPTY;
    }

    private Item filledBucket(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return EItems.PORCELAIN_WATER_BUCKET.get();
        } else if (fluid == Fluids.LAVA) {
            return EItems.PORCELAIN_LAVA_BUCKET.get();
        } else if (fluid == EFluids.WITCH_WATER.get()) {
            return EItems.PORCELAIN_WITCH_WATER_BUCKET.get();
        } else if (fluid == EFluids.MILK.get()) {
            return EItems.PORCELAIN_MILK_BUCKET.get();
        }
        return null;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (currentFluid() != Fluids.EMPTY || maxAmount < 1000 || this.context.getAmount() != 1) {
            return 0;
        }

        var bucket = filledBucket(resource.getFluid());

        if (bucket == null) {
            return 0;
        }

        if (this.context.exchange(ItemVariant.of(bucket), 1, transaction) == 1) {
            return 1000;
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (currentFluid() == Fluids.EMPTY || maxAmount < 1000 || this.context.getAmount() != 1) {
            return 0;
        }

        if (currentFluid() != resource.getFluid()) {
            return 0;
        }

        if (this.context.exchange(ItemVariant.of(EItems.PORCELAIN_BUCKET.get()), 1, transaction) == 1) {
            return 1000;
        }

        return 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        var fluid = currentFluid();
        var variant = fluid == Fluids.EMPTY ? FluidVariant.blank() : FluidVariant.of(fluid);
        return List.<StorageView<FluidVariant>>of(new TankAccess(variant, fluid == Fluids.EMPTY ? 0 : 1000, 1000)).iterator();
    }
}
