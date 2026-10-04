/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import top.starwindv.exdeorum.blockentity.helper.ItemStackHandler;
import top.starwindv.exdeorum.item.WateringCanItem;
import top.starwindv.exdeorum.registry.EBlockEntities;
import top.starwindv.exdeorum.registry.EItems;

// Registers the block entity item/fluid storages and item fluid storages
// with the Fabric Transfer API. Replaces NeoForge capability registration.
public final class ExDeorumCapabilities {
    public static void register() {
        ItemStorage.SIDED.registerForBlockEntity((barrel, direction) -> new ItemStackHandlerStorage(barrel.getItemHandler()), EBlockEntities.BARREL.get());
        FluidStorage.SIDED.registerForBlockEntity((barrel, direction) -> new FluidTankStorage(barrel.getTank()), EBlockEntities.BARREL.get());

        ItemStorage.SIDED.registerForBlockEntity((sieve, direction) -> new ItemStackHandlerStorage(sieve.inventory), EBlockEntities.MECHANICAL_SIEVE.get());
        ItemStorage.SIDED.registerForBlockEntity((hammer, direction) -> new ItemStackHandlerStorage(hammer.inventory), EBlockEntities.MECHANICAL_HAMMER.get());

        ItemStorage.SIDED.registerForBlockEntity((crucible, direction) -> new ItemStackHandlerStorage(crucible.getItem()), EBlockEntities.LAVA_CRUCIBLE.get());
        FluidStorage.SIDED.registerForBlockEntity((crucible, direction) -> new FluidTankStorage(crucible.getTank()), EBlockEntities.LAVA_CRUCIBLE.get());

        ItemStorage.SIDED.registerForBlockEntity((crucible, direction) -> new ItemStackHandlerStorage(crucible.getItem()), EBlockEntities.WATER_CRUCIBLE.get());
        FluidStorage.SIDED.registerForBlockEntity((crucible, direction) -> new FluidTankStorage(crucible.getTank()), EBlockEntities.WATER_CRUCIBLE.get());

        for (var item : EItems.PORCELAIN_BUCKETS) {
            FluidStorage.ITEM.registerForItems((variant, context) -> new PorcelainBucketStorage(context), item.get());
        }

        for (var item : EItems.WATERING_CANS) {
            FluidStorage.ITEM.registerForItems((variant, context) -> new WateringCanStorage(context), item.get());
        }
    }
}
