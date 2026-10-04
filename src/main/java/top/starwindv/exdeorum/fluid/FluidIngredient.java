/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.fluid;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

// Port of NeoForge's FluidIngredient: matches a fluid by id or tag, without an amount.
// Reads the same JSON forms the NeoForge version produced.
public final class FluidIngredient implements Predicate<Fluid> {
    private static final Codec<FluidIngredient> DIRECT_CODEC = BuiltInRegistries.FLUID.byNameCodec()
            .xmap(fluid -> new FluidIngredient(fluid, null), ingredient -> ingredient.fluid);
    private static final Codec<FluidIngredient> TAG_CODEC = Identifier.CODEC
            .xmap(id -> new FluidIngredient(null, TagKey.create(Registries.FLUID, id)), ingredient -> ingredient.tag.location());

    // Plain fluid ids decode as fluids (the common form); tags use the "#tag" string form.
    public static final Codec<FluidIngredient> CODEC = Codec.either(DIRECT_CODEC, TAG_CODEC).xmap(
            either -> either.map(direct -> direct, tag -> tag),
            ingredient -> ingredient.tag != null ? com.mojang.datafixers.util.Either.right(ingredient) : com.mojang.datafixers.util.Either.left(ingredient));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = StreamCodec.of(FluidIngredient::write, FluidIngredient::decode);

    public static final FluidIngredient EMPTY = new FluidIngredient(Fluids.EMPTY, null);

    private final Fluid fluid;
    private final TagKey<Fluid> tag;

    private FluidIngredient(Fluid fluid, TagKey<Fluid> tag) {
        this.fluid = fluid;
        this.tag = tag;
    }

    public static FluidIngredient of(Fluid fluid) {
        return new FluidIngredient(fluid, null);
    }

    public static FluidIngredient of(TagKey<Fluid> tag) {
        return new FluidIngredient(null, tag);
    }

    @Override
    public boolean test(Fluid fluid) {
        if (this.tag != null) {
            return fluid != null && fluid.builtInRegistryHolder().is(this.tag);
        }
        return fluid == this.fluid;
    }

    public boolean isEmpty() {
        return this.tag == null && this.fluid == Fluids.EMPTY;
    }

    // The single fluid this ingredient matches, or null when tag-based
    public Fluid asFluid() {
        return this.tag == null ? this.fluid : null;
    }

    private static void write(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
        buf.writeBoolean(ingredient.tag != null);

        if (ingredient.tag != null) {
            buf.writeIdentifier(ingredient.tag.location());
        } else {
            ByteBufCodecs.registry(BuiltInRegistries.FLUID.key()).encode(buf, ingredient.fluid);
        }
    }

    private static FluidIngredient decode(RegistryFriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return new FluidIngredient(null, TagKey.create(Registries.FLUID, buf.readIdentifier()));
        }
        return new FluidIngredient(ByteBufCodecs.registry(BuiltInRegistries.FLUID.key()).decode(buf), null);
    }
}
