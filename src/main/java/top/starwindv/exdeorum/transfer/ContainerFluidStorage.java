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
 * <p>Only whole volumes move. The Transfer API may ask for less than a volume when the other
 * side of the transfer is short on fluid, and refusing in that case is deliberate: it stops a
 * container from being left holding an amount its own item variant cannot represent. The same
 * goes for a barrel that cannot free up a whole volume.
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

        if (fluid == null || fluid != Fluids.EMPTY || maxAmount < this.container.volume()) {
            return 0;
        }

        var filled = this.container.filledItem(resource.getFluid());

        if (filled == null) {
            return 0;
        }

        if (this.context.exchange(ItemVariant.of(filled), 1, transaction) == 1) {
            return this.container.volume();
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var fluid = currentFluid();

        if (fluid == null || fluid == Fluids.EMPTY || maxAmount < this.container.volume() || fluid != resource.getFluid()) {
            return 0;
        }

        if (this.context.exchange(ItemVariant.of(this.container.emptyItem()), 1, transaction) == 1) {
            return this.container.volume();
        }

        return 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        var fluid = currentFluid();
        var full = fluid != null && fluid != Fluids.EMPTY;
        var variant = full ? FluidVariant.of(fluid) : FluidVariant.blank();
        return List.<StorageView<FluidVariant>>of(new TankAccess(this, variant, full ? this.container.volume() : 0, this.container.volume())).iterator();
    }
}