/*
 * Ex Deorum
 * Copyright (c) 2024 thedarkcolour
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Modifications Copyleft (c) 2026 StarWindv
 * Ported to Fabric
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.recipe;

import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import top.starwindv.exdeorum.fluid.FluidIngredient;
import top.starwindv.exdeorum.fluid.FluidStack;
import top.starwindv.exdeorum.fluid.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class CodecUtil {
    public static final Codec<FluidStack> FLUIDSTACK_CODEC = FluidStack.CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, NumberProvider> NUMBER_PROVIDER_CODEC = StreamCodec.of(RecipeUtil::toNetworkNumberProvider, RecipeUtil::fromNetworkNumberProvider);

    // NeoForge decoded a plain string as a direct fluid and a "#fluid_tag" string as a tag.
    // The ported FluidIngredient.CODEC tries the tag form first, which would misparse plain
    // fluid ids (e.g. "minecraft:milk") as tags, so recipes decode fluid ingredients with these.
    public static final Codec<FluidIngredient> FLUID_INGREDIENT_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<FluidIngredient, T>> decode(DynamicOps<T> ops, T input) {
            return Codec.STRING.decode(ops, input).flatMap(pair -> {
                var ingredient = parseFluidIngredient(pair.getFirst());

                if (ingredient == null) {
                    return DataResult.error(() -> "Unknown fluid or fluid tag: " + pair.getFirst());
                } else {
                    return DataResult.success(Pair.of(ingredient, pair.getSecond()));
                }
            });
        }

        @Override
        public <T> DataResult<T> encode(FluidIngredient input, DynamicOps<T> ops, T prefix) {
            return FluidIngredient.CODEC.encodeStart(ops, input);
        }
    };
    // Same JSON form as SizedFluidIngredient.CODEC, but the inner ingredient is decoded with FLUID_INGREDIENT_CODEC.
    public static final Codec<SizedFluidIngredient> SIZED_FLUID_INGREDIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FLUID_INGREDIENT_CODEC.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
            Codec.INT.fieldOf("amount").forGetter(SizedFluidIngredient::amount)
    ).apply(instance, SizedFluidIngredient::new));

    @Nullable
    private static FluidIngredient parseFluidIngredient(String string) {
        if (string.startsWith("#")) {
            var id = Identifier.tryParse(string.substring(1));

            return id == null ? null : FluidIngredient.of(TagKey.create(Registries.FLUID, id));
        } else {
            var id = Identifier.tryParse(string);

            // containsKey is needed because the fluid registry is defaulted (getValue falls back to water)
            if (id == null || !BuiltInRegistries.FLUID.containsKey(id)) {
                return null;
            }

            return FluidIngredient.of(BuiltInRegistries.FLUID.getValue(id));
        }
    }

    public static <T extends SingleIngredientRecipe> App<RecordCodecBuilder.Mu<T>, Ingredient> ingredientField() {
        return Ingredient.CODEC.fieldOf("ingredient").forGetter(SingleIngredientRecipe::ingredient);
    }

    public static <T> App<RecordCodecBuilder.Mu<T>, Block> blockField(String name, Function<T, Block> getter) {
        return BuiltInRegistries.BLOCK.byNameCodec().fieldOf(name).forGetter(getter);
    }

    public static <T> App<RecordCodecBuilder.Mu<T>, Fluid> fluidField(String name, Function<T, Fluid> getter) {
        return BuiltInRegistries.FLUID.byNameCodec().fieldOf(name).forGetter(getter);
    }

    public static <T> JsonElement encode(Codec<T> codec, T object) {
        return codec.encodeStart(JsonOps.INSTANCE, object).result().get();
    }

    public static <T> T decode(Codec<T> codec, JsonElement json) {
        return codec.parse(JsonOps.INSTANCE, json).result().get();
    }

    public static <T, U extends T, I> DataResult<Pair<T, I>> cast(DataResult<Pair<U, I>> result) {
        return result.map(pair -> pair.mapFirst(Function.identity()));
    }
}
