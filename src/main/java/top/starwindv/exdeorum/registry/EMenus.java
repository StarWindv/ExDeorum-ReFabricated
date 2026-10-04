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

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.menu.MachineData;
import top.starwindv.exdeorum.menu.MechanicalHammerMenu;
import top.starwindv.exdeorum.menu.MechanicalSieveMenu;

public class EMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ExDeorum.ID);

    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalSieveMenu>> MECHANICAL_SIEVE = MENUS.register("mechanical_sieve", id -> new ExtendedMenuType<>(MechanicalSieveMenu::new, MachineData.STREAM_CODEC));
    public static final DeferredHolder<MenuType<?>, MenuType<MechanicalHammerMenu>> MECHANICAL_HAMMER = MENUS.register("mechanical_hammer", id -> new ExtendedMenuType<>(MechanicalHammerMenu::new, MachineData.STREAM_CODEC));
}
