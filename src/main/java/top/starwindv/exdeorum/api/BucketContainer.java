/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.api;

import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * A {@link FluidContainer} in the shape of vanilla buckets: one empty item and one filled item
 * per supported fluid. Register one with {@link ExDeorumApi#registerBuckets(Item, Map)} to make
 * those items interoperate with Ex Deorum's barrels and crucibles.
 *
 * <p>Any fluid can be given a filled item, including modded ones, so a family is not limited to
 * the vanilla water and lava pair. Fluids with no filled item simply cannot be poured into
 * these containers.
 *
 * <pre>{@code
 * ExDeorumApi.registerBuckets(Items.COPPER_INGOT, Map.of(Fluids.WATER, Items.WATER_BUCKET));
 * }</pre>
 */
public record BucketContainer(Item emptyItem, Map<Fluid, Item> filledItems, long volume) implements FluidContainer {
    public BucketContainer {
        Objects.requireNonNull(emptyItem, "emptyItem");
        Objects.requireNonNull(filledItems, "filledItems");

        if (volume <= 0) {
            throw new IllegalArgumentException("volume must be positive, got " + volume);
        }

        // Defensive copy, so a caller mutating their map later cannot desync fluidOf.
        filledItems = Map.copyOf(filledItems);

        for (var entry : filledItems.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "filledItems key");
            Objects.requireNonNull(entry.getValue(), "filledItems value");

            if (entry.getKey() == Fluids.EMPTY) {
                throw new IllegalArgumentException("map the empty variant with emptyItem(), not with Fluids.EMPTY");
            }
        }
    }

    @Override
    @Nullable
    public Fluid fluidOf(Item item) {
        if (item == this.emptyItem) {
            return Fluids.EMPTY;
        }

        for (var entry : this.filledItems.entrySet()) {
            if (entry.getValue() == item) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    @Nullable
    public Item filledItem(Fluid fluid) {
        return fluid == Fluids.EMPTY ? this.emptyItem : this.filledItems.get(fluid);
    }
}