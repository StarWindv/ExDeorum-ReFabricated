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

package top.starwindv.exdeorum.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;
import top.starwindv.exdeorum.recipe.RecipeUtil;
import top.starwindv.exdeorum.recipe.hammer.HammerRecipe;
import top.starwindv.exdeorum.tag.EItemTags;

public class CompressedHammerLootModifier extends HammerLootModifier {
    public static final MapCodec<CompressedHammerLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> LootModifier.codecStart(inst).apply(inst, CompressedHammerLootModifier::new));

    public CompressedHammerLootModifier(LootItemCondition[] conditionsIn, int priority) {
        super(conditionsIn, priority, EItemTags.COMPRESSED_HAMMER_FORTUNE_BLACKLIST);
    }


    @Override
    protected @Nullable HammerRecipe getRecipe(Item itemForm) {
        return RecipeUtil.getCompressedHammerRecipe(itemForm);
    }
}
