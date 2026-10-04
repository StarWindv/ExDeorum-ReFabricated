/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.loot;

import java.util.List;
import java.util.function.Predicate;

import com.mojang.datafixers.Products.P2;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

// Port of NeoForge's global loot modifier base class.
public abstract class LootModifier {
    protected final LootItemCondition[] conditions;
    protected final int priority;
    private final Predicate<LootContext> combinedConditions;

    protected static <T extends LootModifier> P2<RecordCodecBuilder.Mu<T>, LootItemCondition[], Integer> codecStart(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                LootItemCondition.TYPED_CODEC.listOf().xmap(list -> list.toArray(LootItemCondition[]::new), List::of).fieldOf("conditions").forGetter(lm -> lm.conditions),
                Codec.INT.optionalFieldOf("priority", 1000).forGetter(lm -> lm.priority)
        );
    }

    protected LootModifier(LootItemCondition[] conditions, int priority) {
        this.conditions = conditions;
        this.combinedConditions = AllOfCondition.allOf(List.of(conditions));
        this.priority = priority;
    }

    public final ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        return this.combinedConditions.test(context) ? this.doApply(generatedLoot, context) : generatedLoot;
    }

    public int priority() {
        return this.priority;
    }

    protected abstract ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);
}
