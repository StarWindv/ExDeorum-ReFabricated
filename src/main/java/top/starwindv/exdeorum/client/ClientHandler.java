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

package top.starwindv.exdeorum.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import top.starwindv.exdeorum.ExDeorum;
import top.starwindv.exdeorum.block.InfestedLeavesBlock;
import top.starwindv.exdeorum.client.screen.MechanicalHammerScreen;
import top.starwindv.exdeorum.client.screen.MechanicalSieveScreen;
import top.starwindv.exdeorum.client.ter.BarrelRenderer;
import top.starwindv.exdeorum.client.ter.CompressedSieveRenderer;
import top.starwindv.exdeorum.client.ter.CrucibleRenderer;
import top.starwindv.exdeorum.client.ter.InfestedLeavesRenderer;
import top.starwindv.exdeorum.client.ter.SieveRenderer;
import top.starwindv.exdeorum.fluid.WitchWaterFluid;
import top.starwindv.exdeorum.network.ClientMessageHandler;
import top.starwindv.exdeorum.registry.EBlockEntities;
import top.starwindv.exdeorum.registry.EFluids;
import top.starwindv.exdeorum.registry.EMenus;
import top.starwindv.exdeorum.util.AuxLight;
import top.starwindv.exdeorum.util.PendingBlockEntityLoads;
import top.starwindv.exdeorum.util.SimpleReloadListener;

public class ClientHandler implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        register();
    }

    public static void register() {
        registerMenuScreens();
        registerRenderers();
        registerFluidModels();
        addClientReloadListeners();
        trackShaderPack();
        registerClientSideBlockEntityLoads();

        ClientMessageHandler.registerReceivers();
    }

    // InfestedLeavesBlock is common code, so it cannot ask Iris itself. Mirror the state
    // onto it every tick, because a shader pack can be toggled at any time, and again on
    // resource reload so it is never stale while chunk models are being rebuilt.
    private static void trackShaderPack() {
        refreshShaderPackState();
        ClientTickEvents.END_CLIENT_TICK.register(client -> refreshShaderPackState());
    }

    // Client side counterpart of the drain done in EventHandler#serverTick: block entities
    // received from a server are loaded on the client too, and the light engine relight
    // requests are queued from there as well.
    private static void registerClientSideBlockEntityLoads() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PendingBlockEntityLoads.flush();
            AuxLight.flushRelights();
        });
    }

    private static void refreshShaderPackState() {
        InfestedLeavesBlock.shaderPackInUse = ShaderCompat.isShaderPackInUse();
    }

    private static void registerFluidModels() {
        FluidRenderingRegistry.register(
                EFluids.WITCH_WATER.get(),
                EFluids.WITCH_WATER_FLOWING.get(),
                new FluidModel.Unbaked(
                        new Material(WitchWaterFluid.STILL_TEXTURE),
                        new Material(WitchWaterFluid.FLOWING_TEXTURE),
                        new Material(WitchWaterFluid.OVERLAY_TEXTURE),
                        null
                ));
        // Milk has no textures of its own; reuse the water sprites untinted. Vanilla renamed
        // the flowing sprite to water_flow in 26.2.
        FluidRenderingRegistry.register(
                EFluids.MILK.get(),
                EFluids.MILK_FLOWING.get(),
                new FluidModel.Unbaked(
                        new Material(Identifier.withDefaultNamespace("block/water_still")),
                        new Material(Identifier.withDefaultNamespace("block/water_flow")),
                        new Material(Identifier.withDefaultNamespace("block/water_overlay")),
                        null
                ));
    }

    private static void addClientReloadListeners() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleReloadListener(ExDeorum.loc("client_reload"), _ -> {
                    refreshShaderPackState();
                    RenderUtil.reload();
                }));
    }

    private static void registerMenuScreens() {
        net.minecraft.client.gui.screens.MenuScreens.register(EMenus.MECHANICAL_SIEVE.get(), MechanicalSieveScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(EMenus.MECHANICAL_HAMMER.get(), MechanicalHammerScreen::new);
    }

    private static void registerRenderers() {
        BlockEntityRenderers.register(EBlockEntities.INFESTED_LEAVES.get(), InfestedLeavesRenderer::new);
        BlockEntityRenderers.register(EBlockEntities.BARREL.get(), BarrelRenderer::new);
        BlockEntityRenderers.register(EBlockEntities.LAVA_CRUCIBLE.get(), _ -> new CrucibleRenderer());
        BlockEntityRenderers.register(EBlockEntities.WATER_CRUCIBLE.get(), _ -> new CrucibleRenderer());
        BlockEntityRenderers.register(EBlockEntities.SIEVE.get(), _ -> new SieveRenderer<>(0.75f, 15f));
        BlockEntityRenderers.register(EBlockEntities.MECHANICAL_SIEVE.get(), _ -> new SieveRenderer<>(0.75f, 15f));
        BlockEntityRenderers.register(EBlockEntities.COMPRESSED_SIEVE.get(), _ -> new CompressedSieveRenderer<>(0.5625f, 16f));
    }
}
