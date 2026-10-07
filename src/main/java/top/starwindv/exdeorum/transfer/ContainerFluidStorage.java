/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.transfer;

import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import top.starwindv.exdeorum.api.FluidContainer;

/**
 * Backs a {@link FluidContainer} family as a Fabric Transfer API item fluid storage, which is
 * what lets the barrels and crucibles pour into and out of those items.
 *
 * <p>Only whole volumes move in: filling a container from a tank demands the tank free up a
 * whole volume, since the item cannot represent a partial fill. Pouring out is the reverse —
 * any destination with room takes what it can, the container empties through the normal
 * exchange, and what did not fit is voided.
 *
 * <p>Stacks work the way vanilla buckets do. A unit is taken from the held stack and the filled
 * item goes to the player's inventory, or is dropped at their feet when the inventory is full,
 * so filling one bucket out of a stack of them leaves the rest of the stack in hand.
 */
public class ContainerFluidStorage implements Storage<FluidVariant> {
    private final ContainerItemContext context;
    private final FluidContainer container;

    public ContainerFluidStorage(ContainerItemContext context, FluidContainer container) {
        this.context = context;
        this.container = container;
    }

    @Nullable
    private Fluid currentFluid() {
        return this.container.fluidOf(this.context.getItemVariant().getItem());
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var fluid = currentFluid();
        var volumeDroplets = this.container.volume() * FluidTankStorage.DROPLETS_PER_MB;
        // top.starwindv.exdeorum.ExDeorum.LOGGER.info("[Barrel debug] container insert fluid={} maxAmount={} held={} -> volumeDroplets={}",
        //         resource, maxAmount, this.context.getItemVariant(), volumeDroplets);

        if (fluid == null || fluid != Fluids.EMPTY || maxAmount < volumeDroplets) {
            return 0;
        }

        var filled = this.container.filledItem(resource.getFluid());

        if (filled == null) {
            return 0;
        }

        if (this.context.exchange(ItemVariant.of(filled), 1, transaction) == 1) {
            return volumeDroplets;
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var fluid = currentFluid();

        if (fluid == null || fluid == Fluids.EMPTY || fluid != resource.getFluid() || maxAmount <= 0) {
            return 0;
        }

        // Pouring out no longer demands room for the whole volume: whatever the destination
        // accepts moves, the container still swaps to its empty item through the normal
        // exchange, and what did not fit is voided. These items cannot represent a partial
        // fill, so keeping the remainder is not an option, and refusing the pour entirely
        // until the destination had a whole volume free played badly in practice. A full
        // destination accepts nothing, so a full barrel still refuses by itself.
        var moved = Math.min(maxAmount, this.container.volume() * FluidTankStorage.DROPLETS_PER_MB);

        if (this.context.exchange(ItemVariant.of(this.container.emptyItem()), 1, transaction) == 1) {
            return moved;
        }

        return 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        var fluid = currentFluid();
        var full = fluid != null && fluid != Fluids.EMPTY;
        var variant = full ? FluidVariant.of(fluid) : FluidVariant.blank();
        var volumeDroplets = this.container.volume() * FluidTankStorage.DROPLETS_PER_MB;
        return List.<StorageView<FluidVariant>>of(new TankAccess(this, variant, full ? volumeDroplets : 0, volumeDroplets)).iterator();
    }
}