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

package top.starwindv.exdeorum.event;

import java.util.Locale;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.compat.seeds.SeedSieveRegistry;
import top.starwindv.exdeorum.config.EConfig;
import top.starwindv.exdeorum.fluid.FluidInteractions;
import top.starwindv.exdeorum.item.WateringCanItem;
import top.starwindv.exdeorum.loot.LootModifierManager;
import top.starwindv.exdeorum.material.BarrelMaterial;
import top.starwindv.exdeorum.network.VisualUpdateTracker;
import top.starwindv.exdeorum.recipe.RecipeUtil;
import top.starwindv.exdeorum.registry.EFluids;
import top.starwindv.exdeorum.registry.EItems;
import top.starwindv.exdeorum.util.AuxLight;
import top.starwindv.exdeorum.util.PendingBlockEntityLoads;

public final class EventHandler {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLogin(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STARTING.register(EventHandler::serverStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(EventHandler::serverShutdown);
        ServerTickEvents.END_SERVER_TICK.register(EventHandler::serverTick);
        registerFuelValues();
    }

    // The wooden hammers burn as fuel, like NeoForge's IItemExtension#getBurnTime override did
    private static void registerFuelValues() {
        FuelValueEvents.BUILD.register((builder, context) -> {
            builder.add(EItems.WOODEN_HAMMER.get(), 200);
            builder.add(EItems.COMPRESSED_WOODEN_HAMMER.get(), 200);
        });
    }

    // Loads the per-world server config, like NeoForge's SERVER config type
    private static void serverStarting(MinecraftServer server) {
        EConfig.loadServerConfig(server);
    }

    private static void serverShutdown(MinecraftServer server) {
        RecipeUtil.unload();
    }

    // Called from RecipeManagerMixin right after vanilla recipes and tags have (re)loaded, so
    // the freshly loaded map is handed over directly. The loot modifiers are parsed here as
    // well because their conditions need the same registry access the recipes were decoded
    // with. Rebuilding the Ex Deorum caches here rather than on the next tick means they are
    // never briefly stale after a /reload.
    public static void onRecipesReloaded(RecipeMap recipes, ResourceManager resourceManager, HolderLookup.Provider registries, ProfilerFiller profiler) {
        // Only now are the item registry and the tags fully populated, which is what the seed
        // providers need. Registering them during mod init would race other mods' initializers.
        SeedSieveRegistry.process();
        LootModifierManager.reload(resourceManager, registries, profiler);
        RecipeUtil.reload(recipes);
    }

    // Registration of fluid interactions, mirroring the NeoForge FluidInteractionRegistry calls
    public static void registerFluidInteractions() {
        // Lava next to witch water becomes obsidian (source) or netherrack/cobblestone (flowing)
        FluidInteractions.addInteraction(
                state -> state.is(FluidTags.LAVA),
                new FluidInteractions.InteractionInformation(
                        (level, currentPos, relativePos, currentState) -> level.getFluidState(relativePos).getType().isSame(EFluids.WITCH_WATER.get()),
                        fluidState -> fluidState.isSource() ? Blocks.OBSIDIAN.defaultBlockState() : (EConfig.SERVER.witchWaterNetherrackGenerator.get() ? Blocks.NETHERRACK.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState())
                ));
        // Water next to witch water forms dirt variants, allowing a dirt generator
        var dirtVariants = new BlockState[]{Blocks.DIRT.defaultBlockState(), Blocks.PODZOL.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState()};
        var rng = RandomSource.create();
        FluidInteractions.addInteraction(
                state -> state.getType().isSame(EFluids.WITCH_WATER.get()),
                new FluidInteractions.InteractionInformation(
                        (level, currentPos, relativePos, currentState) -> level.getFluidState(relativePos).is(FluidTags.WATER) && EConfig.SERVER.witchWaterDirtGenerator.get(),
                        fluidState -> Util.getRandom(dirtVariants, rng)
                ));
    }

    // The spawn island/tree generation was part of the void world type, which Ex Deorum no
    // longer ships; other skyblock mods are expected to handle world creation.

    private static void onPlayerLogin(ServerPlayer player) {
        var generator = player.level().getChunkSource().getGenerator();

        // Ex Deorum no longer ships a void world type, so skyblock/void worlds come from other
        // mods. Tries to account for them by name, like SkyBlockBuilder or Void Island Control.
        var generatorName = generator.getClass().getName().toLowerCase(Locale.ROOT);

        if (generatorName.contains("skyblock") || generatorName.contains("void")) {
            var advancement = player.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(ExDeorum.ID, "core/root"));

            if (advancement != null) {
                if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                    player.getAdvancements().award(advancement, "in_void_world");
                    if (EConfig.SERVER.startingTorch.get()) {
                        player.getInventory().add(new ItemStack(Items.TORCH));
                    }
                    if (EConfig.SERVER.startingWateringCan.get()) {
                        player.getInventory().add(WateringCanItem.getFull(EItems.WOODEN_WATERING_CAN));
                    }
                }
            } else {
                ExDeorum.LOGGER.error("Unable to grant player the Void World advancement. Ex Deorum advancements will not show");
            }
        }
    }

    private static void serverTick(MinecraftServer server) {
        // Block entities are finished with loading by now, so anything that needed the
        // surrounding blocks, the recipes or the light engine can safely run.
        PendingBlockEntityLoads.flush();
        AuxLight.flushRelights();
        VisualUpdateTracker.syncVisualUpdates(server);
    }
}
