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
import net.minecraft.world.level.material.Fluids;

// Stand-in for NeoForge's SimpleFluidContent, used by the watering can data component.
public record FluidContent(Fluid fluid, int amount) {
    public static final FluidContent EMPTY = new FluidContent(Fluids.EMPTY, 0);

    public static final Codec<FluidContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidContent::fluid),
            Codec.INT.fieldOf("amount").forGetter(FluidContent::amount)
    ).apply(instance, FluidContent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.FLUID.key()), FluidContent::fluid,
            ByteBufCodecs.VAR_INT, FluidContent::amount,
            FluidContent::new);

    public boolean isEmpty() {
        return this.amount <= 0 || this.fluid == Fluids.EMPTY;
    }
}
