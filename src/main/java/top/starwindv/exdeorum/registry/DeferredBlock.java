/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

// Registration helper mirroring NeoForge's DeferredBlock.
public final class DeferredBlock<I extends Block> extends DeferredHolder<Block, I> {
    DeferredBlock(Identifier id) {
        super(id, Registries.BLOCK);
    }
}
