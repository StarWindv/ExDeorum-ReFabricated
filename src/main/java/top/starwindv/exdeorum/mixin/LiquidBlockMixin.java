/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.starwindv.exdeorum.fluid.FluidInteractions;

@Mixin(LiquidBlock.class)
public class LiquidBlockMixin {
    // Checks Ex Deorum fluid interactions (lava + witch water, water + witch water)
    // before a fluid is scheduled to tick, replicating NeoForge's patched neighborChanged.
    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void exdeorum$fluidInteractions(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block, net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston, CallbackInfo cir) {
        if (FluidInteractions.canInteract(level, pos)) {
            cir.cancel();
        }
    }
}
