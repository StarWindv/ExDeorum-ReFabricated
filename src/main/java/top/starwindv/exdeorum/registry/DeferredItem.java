/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

// Registration helper mirroring NeoForge's DeferredItem.
public final class DeferredItem<I extends Item> extends DeferredHolder<Item, I> {
    DeferredItem(Identifier id) {
        super(id, Registries.ITEM);
    }
}
