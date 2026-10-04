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

package top.starwindv.exdeorum.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import top.starwindv.exdeorum.registry.DeferredHolder;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.recipe.OreChunkRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelCompostRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelFluidMixingRecipe;
import top.starwindv.exdeorum.recipe.barrel.FluidTransformationRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelMixingRecipe;
import top.starwindv.exdeorum.recipe.crook.CrookRecipe;
import top.starwindv.exdeorum.recipe.crucible.CrucibleHeatRecipe;
import top.starwindv.exdeorum.recipe.crucible.CrucibleRecipe;
import top.starwindv.exdeorum.recipe.hammer.CompressedHammerRecipe;
import top.starwindv.exdeorum.recipe.hammer.HammerRecipe;
import top.starwindv.exdeorum.recipe.sieve.CompressedSieveRecipe;
import top.starwindv.exdeorum.recipe.sieve.SieveRecipe;

public class ERecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ExDeorum.ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BarrelCompostRecipe>> BARREL_COMPOST = RECIPE_SERIALIZERS.register("barrel_compost", () -> new RecipeSerializer<>(BarrelCompostRecipe.CODEC, BarrelCompostRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BarrelMixingRecipe>> BARREL_MIXING = RECIPE_SERIALIZERS.register("barrel_mixing", () -> new RecipeSerializer<>(BarrelMixingRecipe.CODEC, BarrelMixingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BarrelFluidMixingRecipe>> BARREL_FLUID_MIXING = RECIPE_SERIALIZERS.register("barrel_fluid_mixing", () -> new RecipeSerializer<>(BarrelFluidMixingRecipe.CODEC, BarrelFluidMixingRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FluidTransformationRecipe>> BARREL_FLUID_TRANSFORMATION = RECIPE_SERIALIZERS.register("barrel_fluid_transformation", () -> new RecipeSerializer<>(FluidTransformationRecipe.CODEC, FluidTransformationRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HammerRecipe>> HAMMER = RECIPE_SERIALIZERS.register("hammer", () -> new RecipeSerializer<>(HammerRecipe.CODEC, HammerRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressedHammerRecipe>> COMPRESSED_HAMMER = RECIPE_SERIALIZERS.register("compressed_hammer", () -> new RecipeSerializer<>(CompressedHammerRecipe.CODEC, CompressedHammerRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrookRecipe>> CROOK = RECIPE_SERIALIZERS.register("crook", () -> new RecipeSerializer<>(CrookRecipe.CODEC, CrookRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrucibleHeatRecipe>> CRUCIBLE_HEAT_SOURCE = RECIPE_SERIALIZERS.register("crucible_heat_source", () -> new RecipeSerializer<>(CrucibleHeatRecipe.CODEC, CrucibleHeatRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrucibleRecipe.Lava>> LAVA_CRUCIBLE = RECIPE_SERIALIZERS.register("lava_crucible", () -> new RecipeSerializer<>(CrucibleRecipe.Lava.CODEC, CrucibleRecipe.Lava.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrucibleRecipe.Water>> WATER_CRUCIBLE = RECIPE_SERIALIZERS.register("water_crucible", () -> new RecipeSerializer<>(CrucibleRecipe.Water.CODEC, CrucibleRecipe.Water.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SieveRecipe>> SIEVE = RECIPE_SERIALIZERS.register("sieve", () -> new RecipeSerializer<>(SieveRecipe.CODEC, SieveRecipe.STREAM_CODEC));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CompressedSieveRecipe>> COMPRESSED_SIEVE = RECIPE_SERIALIZERS.register("compressed_sieve", () -> new RecipeSerializer<>(CompressedSieveRecipe.CODEC, CompressedSieveRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<OreChunkRecipe>> ORE_CHUNK = RECIPE_SERIALIZERS.register("ore_chunk", () -> new RecipeSerializer<>(OreChunkRecipe.CODEC, OreChunkRecipe.STREAM_CODEC));
}
