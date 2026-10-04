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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.recipe.OreChunkRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelCompostRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelFluidMixingRecipe;
import top.starwindv.exdeorum.recipe.barrel.BarrelMixingRecipe;
import top.starwindv.exdeorum.recipe.barrel.FluidTransformationRecipe;
import top.starwindv.exdeorum.recipe.crook.CrookRecipe;
import top.starwindv.exdeorum.recipe.crucible.CrucibleHeatRecipe;
import top.starwindv.exdeorum.recipe.crucible.CrucibleRecipe;
import top.starwindv.exdeorum.recipe.hammer.CompressedHammerRecipe;
import top.starwindv.exdeorum.recipe.hammer.HammerRecipe;
import top.starwindv.exdeorum.recipe.sieve.CompressedSieveRecipe;
import top.starwindv.exdeorum.recipe.sieve.SieveRecipe;

public class ERecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ExDeorum.ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BarrelCompostRecipe>> BARREL_COMPOST = RECIPE_TYPES.register("barrel_compost", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<BarrelMixingRecipe>> BARREL_MIXING = RECIPE_TYPES.register("barrel_mixing", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<BarrelFluidMixingRecipe>> BARREL_FLUID_MIXING = RECIPE_TYPES.register("barrel_fluid_mixing", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<FluidTransformationRecipe>> BARREL_FLUID_TRANSFORMATION = RECIPE_TYPES.register("barrel_fluid_transformation", ERecipeTypes::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrucibleRecipe>> LAVA_CRUCIBLE = RECIPE_TYPES.register("lava_crucible", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrucibleRecipe>> WATER_CRUCIBLE = RECIPE_TYPES.register("water_crucible", ERecipeTypes::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<HammerRecipe>> HAMMER = RECIPE_TYPES.register("hammer", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CompressedHammerRecipe>> COMPRESSED_HAMMER = RECIPE_TYPES.register("compressed_hammer", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrookRecipe>> CROOK = RECIPE_TYPES.register("crook", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrucibleHeatRecipe>> CRUCIBLE_HEAT_SOURCE = RECIPE_TYPES.register("crucible_heat_source", ERecipeTypes::simple);

    public static final DeferredHolder<RecipeType<?>, RecipeType<SieveRecipe>> SIEVE = RECIPE_TYPES.register("sieve", ERecipeTypes::simple);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CompressedSieveRecipe>> COMPRESSED_SIEVE = RECIPE_TYPES.register("compressed_sieve", ERecipeTypes::simple);

    // Equivalent of NeoForge's RecipeType.simple; the DeferredRegister performs the actual registration
    private static <T extends Recipe<?>> RecipeType<T> simple(net.minecraft.resources.Identifier id) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return id.toString();
            }
        };
    }
}
