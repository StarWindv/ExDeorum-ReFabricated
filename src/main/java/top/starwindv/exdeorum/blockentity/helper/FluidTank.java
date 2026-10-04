/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.blockentity.helper;

import java.util.function.Predicate;

import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import top.starwindv.exdeorum.fluid.FluidAction;
import top.starwindv.exdeorum.fluid.FluidStack;

// Equivalent of NeoForge's FluidTank template.
public class FluidTank {
    protected final int capacity;
    protected Predicate<FluidStack> validator;
    protected FluidStack fluid = new FluidStack(Fluids.EMPTY, 0);

    public FluidTank(int capacity) {
        this(capacity, stack -> true);
    }

    public FluidTank(int capacity, Predicate<FluidStack> validator) {
        this.capacity = capacity;
        this.validator = validator;
    }

    public boolean isFluidValid(FluidStack stack) {
        return this.validator.test(stack);
    }

    public int getCapacity() {
        return this.capacity;
    }

    public FluidStack getFluid() {
        return this.fluid;
    }

    public int getFluidAmount() {
        return this.fluid.getAmount();
    }

    public void setFluid(FluidStack fluid) {
        this.fluid = fluid;
    }

    public boolean isEmpty() {
        return this.fluid.isEmpty();
    }

    public int getSpace() {
        return Math.max(0, this.capacity - this.fluid.getAmount());
    }

    public int getTanks() {
        return 1;
    }

    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    public int getTankCapacity(int tank) {
        return getCapacity();
    }

    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) {
            return 0;
        }

        if (action.simulate()) {
            if (this.fluid.isEmpty()) {
                return Math.min(this.capacity, resource.getAmount());
            }
            if (!FluidStack.isSameFluidSameComponents(this.fluid, resource)) {
                return 0;
            }
            return Math.min(this.capacity - this.fluid.getAmount(), resource.getAmount());
        }

        if (this.fluid.isEmpty()) {
            this.fluid = new FluidStack(resource.getFluid(), Math.min(this.capacity, resource.getAmount()));
            onContentsChanged();
            return this.fluid.getAmount();
        }

        if (!FluidStack.isSameFluidSameComponents(this.fluid, resource)) {
            return 0;
        }

        int filled = this.capacity - this.fluid.getAmount();

        if (resource.getAmount() < filled) {
            this.fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            this.fluid.setAmount(this.capacity);
        }

        if (filled > 0) {
            onContentsChanged();
        }

        return filled;
    }

    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, this.fluid)) {
            return FluidStack.EMPTY;
        }
        return drainInternal(resource.getAmount(), action);
    }

    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        return drainInternal(maxDrain, action);
    }

    private FluidStack drainInternal(int maxDrain, FluidAction action) {
        int drained = Math.min(this.fluid.getAmount(), maxDrain);
        var drainedStack = new FluidStack(this.fluid.getFluid(), drained);

        if (drained > 0 && action.execute()) {
            this.fluid.shrink(drained);

            if (this.fluid.isEmpty()) {
                this.fluid = new FluidStack(Fluids.EMPTY, 0);
            }
            onContentsChanged();
        }

        return drainedStack;
    }

    public void deserialize(ValueInput input) {
        this.fluid = input.read("Fluid", FluidStack.CODEC).orElseGet(() -> new FluidStack(Fluids.EMPTY, 0));
    }

    public void serialize(ValueOutput output) {
        if (!this.fluid.isEmpty()) {
            output.store("Fluid", FluidStack.CODEC, this.fluid);
        }
    }

    protected void onContentsChanged() {
    }
}
