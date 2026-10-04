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

import java.util.function.UnaryOperator;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.fluid.FluidContent;

public class EDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), ExDeorum.ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidContent>> WATERING_CAN = register("watering_can", builder -> builder
            .networkSynchronized(FluidContent.STREAM_CODEC)
            .persistent(FluidContent.CODEC)
    );

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String path, UnaryOperator<DataComponentType.Builder<T>> configure) {
        return DATA_COMPONENTS.register(path, () -> configure.apply(new DataComponentType.Builder<>()).build());
    }
}
