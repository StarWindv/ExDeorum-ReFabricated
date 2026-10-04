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

package top.starwindv.exdeorum;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.starwindv.exdeorum.blockentity.helper.ItemHelper;
import top.starwindv.exdeorum.compat.seeds.SeedSieveRegistry;
import top.starwindv.exdeorum.config.EConfig;
import top.starwindv.exdeorum.config.ProbabilityConfig;
import top.starwindv.exdeorum.event.EventHandler;
import top.starwindv.exdeorum.fluid.FluidInteractions;
import top.starwindv.exdeorum.material.BarrelMaterial;
import top.starwindv.exdeorum.material.DefaultMaterials;
import top.starwindv.exdeorum.registry.*;
import top.starwindv.exdeorum.transfer.ExDeorumCapabilities;

import java.util.Calendar;

public class ExDeorum implements ModInitializer {
    public static final String ID = "exdeorum";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final boolean IS_JUNE = Calendar.getInstance().get(Calendar.MONTH) == Calendar.JUNE;

    @Override
    public void onInitialize() {
        // Load common and client configs first, they are needed during registration
        EConfig.loadConfigs();
        ProbabilityConfig.load();

        // Registration order matters: fluids must exist before the fluid blocks
        EFluids.FLUIDS.register();
        DefaultMaterials.registerMaterials();
        ECompressedBlocks.register();
        EBlocks.BLOCKS.register();
        EItems.ITEMS.register();
        EBlockEntities.BLOCK_ENTITIES.register();
        ECreativeTabs.CREATIVE_TABS.register();
        ESounds.SOUNDS.register();
        EMenus.MENUS.register();
        ERecipeSerializers.RECIPE_SERIALIZERS.register();
        ERecipeTypes.RECIPE_TYPES.register();
        ELootFunctions.LOOT_FUNCTIONS.register();
        ENumberProviders.NUMBER_PROVIDERS.register();
        EDataComponents.DATA_COMPONENTS.register();
        // Loads the class so the global loot modifier codecs are registered
        EGlobalLootModifiers.register();

        // Seeds that no vanilla tag covers are sifted from dirt. The providers are only registered
        // here; they are resolved after the first datapack load, see SeedSieveRegistry.
        SeedSieveRegistry.registerBuiltins();

        // Fluid interactions replicate NeoForge's FluidInteractionRegistry registrations
        EventHandler.registerFluidInteractions();
        BarrelMaterial.loadTransparentBlocks();
        CompostColorsLoader.loadColors();
        // Item and fluid storage for hoppers, pipes and other mods
        ExDeorumCapabilities.register();

        // Game Events
        EventHandler.register();
        NetworkRegistrationHelper.register();

        // The client entrypoint (ClientHandler) is invoked by Fabric on the client.
    }

    public static Identifier loc(String menuProperty) {
        return Identifier.fromNamespaceAndPath(ID, menuProperty);
    }
}
