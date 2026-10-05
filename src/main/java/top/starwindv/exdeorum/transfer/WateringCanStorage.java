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
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluids;
import top.starwindv.exdeorum.fluid.FluidContent;
import top.starwindv.exdeorum.item.WateringCanItem;
import top.starwindv.exdeorum.registry.EDataComponents;

// Fluid storage for watering cans. Fill only with water, exactly like
// the NeoForge FluidHandlerItemStack subclass did (drain disabled).
public class WateringCanStorage implements Storage<FluidVariant> {
    private final ContainerItemContext context;

    public WateringCanStorage(ContainerItemContext context) {
        this.context = context;
    }

    private FluidContent contents() {
        return this.context.getItemVariant().getComponents().getOrDefault(EDataComponents.WATERING_CAN.get(), FluidContent.EMPTY);
    }

    private int capacity() {
        var item = this.context.getItemVariant().getItem();

        if (item instanceof WateringCanItem can) {
            return can.getCapacity();
        }

        return 0;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (resource.getFluid() != Fluids.WATER || resource.hasComponents()) {
            return 0;
        }

        var contents = contents();
        var space = capacity() - contents.amount();

        if (space <= 0 || maxAmount <= 0) {
            return 0;
        }

        var filled = (int) Math.min(space, maxAmount);
        var patch = DataComponentPatch.builder().set(EDataComponents.WATERING_CAN.get(), new FluidContent(Fluids.WATER, contents.amount() + filled)).build();
        var newVariant = this.context.getItemVariant().withComponents(patch);

        if (this.context.exchange(newVariant, 1, transaction) == 1) {
            return filled;
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        // watering cans cannot be drained, same as the NeoForge version
        return 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        var contents = contents();
        var variant = contents.isEmpty() ? FluidVariant.blank() : FluidVariant.of(contents.fluid());
        return List.<StorageView<FluidVariant>>of(new TankAccess(this, variant, contents.isEmpty() ? 0 : contents.amount(), capacity())).iterator();
    }
}
