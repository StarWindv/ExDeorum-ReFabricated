/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import top.starwindv.exdeorum.api.BucketContainer;
import top.starwindv.exdeorum.registry.EFluids;
import top.starwindv.exdeorum.registry.EItems;

// Fluid storage for the vanilla buckets. The NeoForge version got this for free from the fluid
// handler capability BucketItem carries there, so the barrels and crucibles need an equivalent
// or vanilla buckets end up treated as ordinary ingredients instead of fluid containers.
public class VanillaBucketStorage extends ContainerFluidStorage {
    private static final BucketContainer CONTAINER = new BucketContainer(Items.BUCKET,
            Map.of(Fluids.WATER, Items.WATER_BUCKET,
                    Fluids.LAVA, Items.LAVA_BUCKET,
                    EFluids.WITCH_WATER.get(), EItems.WITCH_WATER_BUCKET.get()),
            1000);

    public VanillaBucketStorage(ContainerItemContext context) {
        super(context, CONTAINER);
    }
}