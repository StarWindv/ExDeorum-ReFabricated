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

package top.starwindv.exdeorum.data;

import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.loot.CompressedHammerLootModifier;
import top.starwindv.exdeorum.loot.CrookLootModifier;
import top.starwindv.exdeorum.loot.HammerLootModifier;
import top.starwindv.exdeorum.tag.EItemTags;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

class LootModifiers extends GlobalLootModifierProvider {
    LootModifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ExDeorum.ID);
    }

    @Override
    protected void start() {
        add("hammer", HammerLootModifier::new, EItemTags.HAMMERS);
        add("compressed_hammer", CompressedHammerLootModifier::new, EItemTags.COMPRESSED_HAMMERS);
        add("crook", CrookLootModifier::new, EItemTags.CROOKS);
    }

    private void add(String name, BiFunction<LootItemCondition[], Integer, IGlobalLootModifier> constructor, TagKey<Item> requiredTag) {
        add(name, constructor.apply(new LootItemCondition[]{new MatchTool(Optional.of(ItemPredicate.Builder.item().of(this.registries.lookupOrThrow(Registries.ITEM), requiredTag).build()))}, 1), List.of());
    }
}
