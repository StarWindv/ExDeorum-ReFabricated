/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.starwindv.exdeorum.util.AuxLight;

// Makes the light engine aware of the dynamic light registered through AuxLight,
// which replaces NeoForge's AuxLightManager and its patched getLightEmission.
@Mixin(BlockLightEngine.class)
public class BlockLightEngineMixin {
    @Inject(method = "getEmission(JLnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), cancellable = true)
    private void exdeorum$auxLightEmission(long packedPos, BlockState state, CallbackInfoReturnable<Integer> cir) {
        var chunkSource = ((LightEngineAccessor) this).exdeorum$getChunkSource();

        if (chunkSource.getLevel() instanceof Level level) {
            int auxLight = AuxLight.getLightAt(level, packedPos);

            if (auxLight > 0) {
                cir.setReturnValue(Math.max(auxLight, state.getLightEmission()));
            }
        }
    }
}
