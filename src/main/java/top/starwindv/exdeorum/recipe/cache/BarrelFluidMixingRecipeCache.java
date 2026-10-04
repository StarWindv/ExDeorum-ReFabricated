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

package top.starwindv.exdeorum.recipe.cache;

import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import top.starwindv.exdeorum.recipe.RecipeUtil;
import top.starwindv.exdeorum.recipe.barrel.BarrelFluidMixingRecipe;
import top.starwindv.exdeorum.registry.ERecipeTypes;

import java.util.HashMap;
import java.util.Map;

// for now, only simple recipes
public class BarrelFluidMixingRecipeCache {
    private RecipeMap recipeManager;
    @Nullable
    private Map<Fluid, Map<Fluid, BarrelFluidMixingRecipe>> recipes;

    public BarrelFluidMixingRecipeCache(RecipeMap recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Nullable
    public BarrelFluidMixingRecipe getRecipe(Fluid baseFluid, Fluid additive) {
        if (this.recipes == null) {
            buildRecipes();
        }
        var recipesForBase = this.recipes.get(baseFluid);
        if (recipesForBase != null) {
            return recipesForBase.get(additive);
        }
        return null;
    }

    private void buildRecipes() {
        this.recipes = new HashMap<>();

        for (var holder : this.recipeManager.byType(ERecipeTypes.BARREL_FLUID_MIXING.get())) {
            var recipe = holder.value();
            for (var baseFluid : RecipeUtil.getMatchingFluids(recipe.baseFluid().ingredient())) {
                var map = this.recipes.computeIfAbsent(baseFluid, key -> new HashMap<>());

                for (var additiveFluid : RecipeUtil.getMatchingFluids(recipe.additiveFluid())) {
                    map.put(additiveFluid, recipe);
                }
            }
        }

        this.recipeManager = null;
    }
}
