/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Describes a family of items that hold a fixed volume of one fluid, in the vanilla bucket
 * style: the contents live in the item itself, so one unit of a stack holds exactly one
 * {@link #volume()} and filling or emptying one unit swaps it for another item of the family.
 *
 * <p>Register one with {@link ExDeorumApi#registerFluidContainer(FluidContainer, Item...)} to let
 * those items pour into and take fluid out of Ex Deorum's barrels and crucibles. Transfers move
 * a whole volume at a time, which is what keeps an item from ending up in a state its own
 * variant cannot represent: the barrel needs room for the full volume to accept it, and needs
 * at least the full volume to fill a container. A barrel that cannot take or supply one whole
 * volume simply refuses the interaction instead of half filling a container.
 *
 * <p>Most bucket shaped items can just use {@link BucketContainer}. Implement this yourself when
 * a family has more than one item per fluid, or when the mapping is computed rather than
 * written out. Containers whose fill level varies instead of living in the item, such as Ex
 * Deorum's watering cans, do not belong here; they register their own
 * {@link net.fabricmc.fabric.api.transfer.v1.storage.Storage} on
 * {@link net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage#ITEM} instead, which stays
 * supported and is the more general route.
 *
 * <p>This only adds interoperability with Ex Deorum's own fluid containers. Handing fluid to a
 * world block, or picking one up, is a separate concern that the item still has to implement
 * itself, as vanilla buckets do.
 */
public interface FluidContainer {
    /**
     * How much fluid one unit holds, in mB. Must be positive.
     */
    long volume();

    /**
     * The fluid the given item currently holds.
     *
     * @param item one of the items the container was registered for
     * @return {@link Fluids#EMPTY} for the empty variant, or {@code null} if the item is not
     *         part of this family
     */
    @Nullable
    Fluid fluidOf(Item item);

    /**
     * The item this family uses when it holds nothing.
     */
    Item emptyItem();

    /**
     * The item of this family that holds {@code fluid}.
     *
     * @return {@code null} when this container cannot hold that fluid
     */
    @Nullable
    Item filledItem(Fluid fluid);
}