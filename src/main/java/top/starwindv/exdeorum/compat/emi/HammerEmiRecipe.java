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

package top.starwindv.exdeorum.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;
import top.starwindv.exdeorum.recipe.hammer.HammerRecipe;

import java.util.List;

abstract class HammerEmiRecipe extends EmiOneToOneRecipe {
    private final List<EmiStack> outputs;

    HammerEmiRecipe(HammerRecipe recipe, Identifier id) {
        super(recipe, id);

        this.outputs = EmiUtil.outputs(recipe.result);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return this.outputs;
    }

    static class Hammer extends HammerEmiRecipe {
        Hammer(HammerRecipe recipe, Identifier id) {
            super(recipe, id);
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return ExDeorumEmiPlugin.HAMMER;
        }
    }

    static class CompressedHammer extends HammerEmiRecipe {
        CompressedHammer(HammerRecipe recipe, Identifier id) {
            super(recipe, id);
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return ExDeorumEmiPlugin.COMPRESSED_HAMMER;
        }
    }
}
