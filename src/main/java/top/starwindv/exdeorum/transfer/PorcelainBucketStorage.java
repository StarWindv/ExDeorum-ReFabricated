/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.level.material.Fluids;
import top.starwindv.exdeorum.api.BucketContainer;
import top.starwindv.exdeorum.registry.EFluids;
import top.starwindv.exdeorum.registry.EItems;

// Fluid storage for the porcelain buckets, which carry their fluid in the item itself
// exactly like vanilla buckets do.
public class PorcelainBucketStorage extends ContainerFluidStorage {
    private static final BucketContainer CONTAINER = new BucketContainer(EItems.PORCELAIN_BUCKET.get(),
            Map.of(Fluids.WATER, EItems.PORCELAIN_WATER_BUCKET.get(),
                    Fluids.LAVA, EItems.PORCELAIN_LAVA_BUCKET.get(),
                    EFluids.WITCH_WATER.get(), EItems.PORCELAIN_WITCH_WATER_BUCKET.get(),
                    EFluids.MILK.get(), EItems.PORCELAIN_MILK_BUCKET.get()),
            1000);

    public PorcelainBucketStorage(ContainerItemContext context) {
        super(context, CONTAINER);
    }
}
