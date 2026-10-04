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

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import top.starwindv.exdeorum.compat.XeiSieveRecipe;
import top.starwindv.exdeorum.util.TranslationKeys;
import top.starwindv.exdeorum.material.DefaultMaterials;

class CompressedSieveCategory extends SieveCategory {
    CompressedSieveCategory(IGuiHelper helper) {
        super(helper, DefaultMaterials.OAK_COMPRESSED_SIEVE, Component.translatable(TranslationKeys.COMPRESSED_SIEVE_CATEGORY_TITLE), XeiSieveRecipe.COMPRESSED_SIEVE_ROWS);
    }

    @Override
    public IRecipeType<XeiSieveRecipe> getRecipeType() {
        return ExDeorumJeiPlugin.COMPRESSED_SIEVE;
    }
}
