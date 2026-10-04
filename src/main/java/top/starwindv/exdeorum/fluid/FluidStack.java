/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.fluid;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

// Minimal fluid stack carrying just a fluid and an amount in milli-buckets.
// The fluids used by Ex Deorum never carry data components.
public class FluidStack {
    public static final Codec<FluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidStack::getFluid),
            Codec.INT.fieldOf("amount").forGetter(FluidStack::getAmount)
    ).apply(instance, FluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.FLUID.key()), FluidStack::getFluid,
            ByteBufCodecs.VAR_INT, FluidStack::getAmount,
            FluidStack::new
    );

    // Visually synced fluid may be absent (empty tank), so an optional wrapper is needed.
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidStack decode(RegistryFriendlyByteBuf buf) {
            if (!buf.readBoolean()) {
                return new FluidStack(Fluids.EMPTY, 0);
            }
            return STREAM_CODEC.decode(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
            var empty = stack.isEmpty();
            buf.writeBoolean(!empty);

            if (!empty) {
                STREAM_CODEC.encode(buf, stack);
            }
        }
    };

    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    private Fluid fluid;
    private int amount;

    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }

    public Fluid getFluid() {
        return this.fluid;
    }

    public int getAmount() {
        return this.amount;
    }

    public boolean isEmpty() {
        return this.amount <= 0 || this.fluid == Fluids.EMPTY;
    }

    public void setFluid(Fluid fluid) {
        this.fluid = fluid;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void grow(int amount) {
        this.amount += amount;
    }

    public void shrink(int amount) {
        this.amount = Math.max(0, this.amount - amount);
    }

    public FluidStack copy() {
        return new FluidStack(this.fluid, this.amount);
    }

    // Equivalent of NeoForge's FluidStack.isSameFluidSameComponents for component-less fluids
    public static boolean isSameFluidSameComponents(FluidStack a, FluidStack b) {
        return a.fluid == b.fluid;
    }

    public boolean is(Fluid fluid) {
        return this.fluid == fluid;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj instanceof FluidStack stack) {
            return this.amount == stack.amount && this.fluid == stack.fluid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fluid, this.amount);
    }

    @Override
    public String toString() {
        return this.amount + " mB of " + this.fluid;
    }
}
