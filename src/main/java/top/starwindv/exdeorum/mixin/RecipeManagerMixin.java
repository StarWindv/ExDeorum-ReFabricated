/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.mixin;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.starwindv.exdeorum.event.EventHandler;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow
    private net.minecraft.core.HolderLookup.Provider registries;

    // After recipes (and tags) have (re)loaded at world start or on /reload, rebuild the
    // Ex Deorum static recipe caches and reload the global loot modifiers, replicating
    // NeoForge's TagsUpdatedEvent + OnDatapackSync handling.
    @Inject(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("TAIL"))
    private void exdeorum$onRecipesApplied(RecipeMap recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo cir) {
        EventHandler.onRecipesReloaded(recipes, resourceManager, registries, profiler);
    }
}