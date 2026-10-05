/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.item.Items;
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

        // NeoForge gave the vanilla buckets a fluid handler capability, so the barrels and
        // crucibles have to recognise them here or they would treat them as plain ingredients.
        // Ex Deorum's witch water bucket is a vanilla BucketItem too, so it joins them.
        for (var item : List.of(Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET, EItems.WITCH_WATER_BUCKET.get())) {
            FluidStorage.ITEM.registerForItems((variant, context) -> new VanillaBucketStorage(context), item);
        }

        for (var item : EItems.WATERING_CANS) {
            FluidStorage.ITEM.registerForItems((variant, context) -> new WateringCanStorage(context), item.get());
        }
    }
}
