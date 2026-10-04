/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

// Port of NeoForge's FluidStackTemplate: the fluid result of a crucible recipe.
// Reads the same JSON form the NeoForge version produced: {"id": "minecraft:lava", "amount": 250}
public record FluidStackTemplate(Fluid fluid, int amount) {
    public static final Codec<FluidStackTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidStackTemplate::fluid),
            Codec.INT.fieldOf("amount").forGetter(FluidStackTemplate::amount)
    ).apply(instance, FluidStackTemplate::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.FLUID.key()), FluidStackTemplate::fluid,
            ByteBufCodecs.VAR_INT, FluidStackTemplate::amount,
            FluidStackTemplate::new);

    public FluidStack create() {
        return new FluidStack(this.fluid, this.amount);
    }

    public FluidStack create(int times) {
        return new FluidStack(this.fluid, this.amount * times);
    }
}
