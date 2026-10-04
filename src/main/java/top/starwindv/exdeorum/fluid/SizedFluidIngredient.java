/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

// Port of NeoForge's SizedFluidIngredient: a FluidIngredient plus a required amount.
// JSON form: {"amount": 1000, "ingredient": "minecraft:water"}
public record SizedFluidIngredient(FluidIngredient ingredient, int amount) {
    public static final Codec<SizedFluidIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidIngredient.CODEC.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
            Codec.INT.fieldOf("amount").forGetter(SizedFluidIngredient::amount)
    ).apply(instance, SizedFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC = StreamCodec.composite(
            FluidIngredient.STREAM_CODEC, SizedFluidIngredient::ingredient,
            net.minecraft.network.codec.ByteBufCodecs.VAR_INT, SizedFluidIngredient::amount,
            SizedFluidIngredient::new);

    public static SizedFluidIngredient of(Fluid fluid, int amount) {
        return new SizedFluidIngredient(FluidIngredient.of(fluid), amount);
    }

    // Whether the given fluid matches and the available amount covers the requirement
    public boolean test(Fluid fluid, int availableAmount) {
        return this.ingredient.test(fluid) && availableAmount >= this.amount;
    }

    public boolean test(FluidStack stack) {
        return !stack.isEmpty() && test(stack.getFluid(), stack.getAmount());
    }

    public boolean isEmpty() {
        return this.amount == 0 || this.ingredient.isEmpty();
    }

    // The matched fluid for plain-id ingredients (crucible results), null when tag-based
    public Fluid fluid() {
        return this.ingredient.asFluid();
    }
}
