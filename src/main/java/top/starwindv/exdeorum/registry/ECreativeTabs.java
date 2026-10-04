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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.util.TranslationKeys;

public class ECreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExDeorum.ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main", id -> {
        var builder = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0);
        mainTab(builder);
        return builder.build();
    });

    private static void mainTab(CreativeModeTab.Builder builder) {
        builder.icon(() -> new ItemStack(EItems.CROOK.get()));
        builder.title(Component.translatable(TranslationKeys.MAIN_CREATIVE_TAB));
        // Vanilla 26.2 has no tab ordering API; Fabric API places modded tabs after the vanilla ones
        builder.displayItems((enabledFeatures, output) -> EItems.addItemsToMainTab(output));
    }
}
