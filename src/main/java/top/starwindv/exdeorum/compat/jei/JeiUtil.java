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

package top.starwindv.exdeorum.compat.jei;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import top.starwindv.exdeorum.fluid.FluidIngredient;
import top.starwindv.exdeorum.fluid.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

class JeiUtil {
    // JEI on Fabric measures fluids in droplets (81000 droplets per bucket) while
    // Ex Deorum recipes and tanks are measured in mB, so convert when displaying
    private static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000;

    public static long toDroplets(int mB) {
        return mB * DROPLETS_PER_MB;
    }

    public static IRecipeSlotBuilder addFluidIngredient(IRecipeSlotBuilder builder, SizedFluidIngredient fluid) {
        return addFluidIngredient(builder, fluid.ingredient(), fluid.amount());
    }

    public static IRecipeSlotBuilder addFluidIngredient(IRecipeSlotBuilder builder, FluidIngredient ingredient, int amount) {
        for (var fluid : getPossibleFluids(ingredient)) {
            builder.add(fluid, toDroplets(amount));
        }
        return builder;
    }

    // Ex Deorum's FluidIngredient is either a single fluid or a fluid tag,
    // so collect every registered fluid it accepts (like NeoForge's FluidIngredient.fluids())
    private static List<Fluid> getPossibleFluids(FluidIngredient ingredient) {
        if (ingredient.isEmpty()) {
            return List.of();
        }
        var fluids = new ArrayList<Fluid>();
        for (var fluid : BuiltInRegistries.FLUID) {
            if (fluid != Fluids.EMPTY && ingredient.test(fluid)) {
                fluids.add(fluid);
            }
        }
        return fluids;
    }
}
