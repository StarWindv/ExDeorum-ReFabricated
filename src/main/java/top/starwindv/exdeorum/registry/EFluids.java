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
import net.minecraft.world.level.material.Fluid;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.fluid.MilkFluid;
import top.starwindv.exdeorum.fluid.WitchWaterFluid;

public class EFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, ExDeorum.ID);

    public static final DeferredHolder<Fluid, WitchWaterFluid.Source> WITCH_WATER = FLUIDS.register("witch_water", id -> new WitchWaterFluid.Source());
    public static final DeferredHolder<Fluid, WitchWaterFluid.Flowing> WITCH_WATER_FLOWING = FLUIDS.register("flowing_witch_water", id -> new WitchWaterFluid.Flowing());

    // Milk is a real fluid on NeoForge, so a fluid is registered here for parity.
    // Only used by barrel fluid mixing recipes and the porcelain milk bucket.
    public static final DeferredHolder<Fluid, MilkFluid.Source> MILK = FLUIDS.register("milk", id -> new MilkFluid.Source());
    public static final DeferredHolder<Fluid, MilkFluid.Flowing> MILK_FLOWING = FLUIDS.register("flowing_milk", id -> new MilkFluid.Flowing());
}
