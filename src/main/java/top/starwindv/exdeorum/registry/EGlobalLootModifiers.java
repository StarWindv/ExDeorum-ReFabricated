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

import com.mojang.serialization.MapCodec;
import top.starwindv.exdeorum.loot.CompressedHammerLootModifier;
import top.starwindv.exdeorum.loot.CrookLootModifier;
import top.starwindv.exdeorum.loot.HammerLootModifier;
import top.starwindv.exdeorum.loot.LootModifierManager;

public class EGlobalLootModifiers {
    public static final MapCodec<CrookLootModifier> CROOK = LootModifierManager.register("crook", CrookLootModifier.CODEC);
    public static final MapCodec<HammerLootModifier> HAMMER = LootModifierManager.register("hammer", HammerLootModifier.CODEC);
    public static final MapCodec<CompressedHammerLootModifier> COMPRESSED_HAMMER = LootModifierManager.register("compressed_hammer", CompressedHammerLootModifier.CODEC);

    // Loading this class registers the codecs above with LootModifierManager,
    // so it is touched from the mod entrypoint before any loot modifiers are read.
    public static void register() {
    }
}
