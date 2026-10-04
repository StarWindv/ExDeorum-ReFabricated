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
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.starwindv.exdeorum.util.TranslationKeys;
import top.starwindv.exdeorum.material.DefaultMaterials;
import top.starwindv.exdeorum.recipe.crucible.CrucibleRecipe;

abstract class CrucibleCategory extends OneToOneCategory<CrucibleRecipe> {
    public CrucibleCategory(IGuiHelper helper, IDrawable arrow, Item iconItem, String titleKey) {
        super(helper, arrow, helper.createDrawableItemStack(new ItemStack(iconItem)), Component.translatable(titleKey));
    }

    @Override
    protected void addInput(IRecipeSlotBuilder slot, CrucibleRecipe recipe) {
        slot.add(recipe.ingredient());
    }

    @Override
    protected void addOutput(IRecipeSlotBuilder slot, CrucibleRecipe recipe) {
        var result = recipe.getResult();
        slot.add(result.fluid(), JeiUtil.toDroplets(result.amount()))
                .setFluidRenderer(JeiUtil.toDroplets(Math.max(1000, result.amount())), false, 16, 16);
    }

    static class LavaCrucible extends CrucibleCategory {
        public LavaCrucible(IGuiHelper helper, IDrawable arrow) {
            super(helper, arrow, DefaultMaterials.PORCELAIN_CRUCIBLE.getItem(), TranslationKeys.LAVA_CRUCIBLE_CATEGORY_TITLE);
        }

        @Override
        public IRecipeType<CrucibleRecipe> getRecipeType() {
            return ExDeorumJeiPlugin.LAVA_CRUCIBLE;
        }
    }

    static class WaterCrucible extends CrucibleCategory {
        public WaterCrucible(IGuiHelper helper, IDrawable arrow) {
            super(helper, arrow, DefaultMaterials.OAK_CRUCIBLE.getItem(), TranslationKeys.WATER_CRUCIBLE_CATEGORY_TITLE);
        }

        @Override
        public IRecipeType<CrucibleRecipe> getRecipeType() {
            return ExDeorumJeiPlugin.WATER_CRUCIBLE;
        }
    }
}
