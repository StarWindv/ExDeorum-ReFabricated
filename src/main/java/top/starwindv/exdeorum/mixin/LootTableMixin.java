/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.starwindv.exdeorum.loot.LootModifierManager;

@Mixin(LootTable.class)
public class LootTableMixin {
    // Applies Ex Deorum's global loot modifiers to generated loot (crook and hammers).
    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    private ObjectArrayList<ItemStack> exdeorum$modifyLoot(ObjectArrayList<ItemStack> loot, LootContext context) {
        return LootModifierManager.modifyLoot(loot, context);
    }
}
