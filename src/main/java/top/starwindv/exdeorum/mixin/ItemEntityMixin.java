/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.mixin;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.starwindv.exdeorum.item.SilkwormItem;
import top.starwindv.exdeorum.registry.ESounds;

// Replaces NeoForge's IItemExtension#onEntityItemUpdate hook for silk worms:
// play a gross noise when you discover a silk worm.
@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void exdeorum$silkwormDropSound(CallbackInfo ci) {
        var entity = (ItemEntity) (Object) this;

        // tickCount is 0 on the first tick of an item entity's life
        if (entity.tickCount == 0 && entity.getOwner() == null) {
            ItemStack stack = entity.getItem();

            if (stack.getItem() instanceof SilkwormItem) {
                entity.level().playSound(null, entity, ESounds.SILK_WORM_DROP.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }
}
