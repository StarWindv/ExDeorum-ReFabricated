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

package top.starwindv.exdeorum.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import top.starwindv.exdeorum.registry.DeferredBlock;
import top.starwindv.exdeorum.registry.DeferredItem;
import top.starwindv.exdeorum.compat.ModIds;
import top.starwindv.exdeorum.registry.EBlocks;
import top.starwindv.exdeorum.registry.EItems;
import top.starwindv.exdeorum.tag.EItemTags;

import java.util.function.Supplier;

public class CompressedBlockType implements ItemLike {
    private final DeferredBlock<Block> block;
    private final DeferredItem<BlockItem> item;
    private final TagKey<Item> itemTag;
    private final Supplier<Block> base;

    private boolean hasCompressium;

    public CompressedBlockType(String name, Supplier<Block> base) {
        this.block = EBlocks.BLOCKS.register("compressed_" + name, this::createBlock);
        this.item = EItems.registerItemBlock(this.block);
        this.itemTag = EItemTags.tag("compressed/" + name);
        this.base = base;
    }

    private Block createBlock(Identifier id) {
        return new Block(BlockBehaviour.Properties.ofFullCopy(this.base.get()).setId(ResourceKey.create(Registries.BLOCK, id)));
    }

    public Block getBlock() {
        return this.block.get();
    }

    public BlockItem getItem() {
        return this.item.get();
    }

    @Override
    public Item asItem() {
        return this.item.get();
    }

    public TagKey<Item> getTag() {
        return this.itemTag;
    }

    public CompressedBlockType withCompressium() {
        this.hasCompressium = true;
        return this;
    }

    // AllTheCompressed has every vanilla block that Ex Deorum uses so far
    public boolean hasAtc() {
        return true;
    }

    public boolean hasCompressium() {
        return this.hasCompressium;
    }

    public Identifier getAtc() {
        return Identifier.fromNamespaceAndPath(ModIds.ALL_THE_COMPRESSED, BuiltInRegistries.BLOCK.getKey(this.base.get()).getPath() + "_1x");
    }

    public Identifier getCompressium() {
        return Identifier.fromNamespaceAndPath(ModIds.COMPRESSIUM, BuiltInRegistries.BLOCK.getKey(this.base.get()).getPath() + "_1");
    }

    public Block getBase() {
        return this.base.get();
    }

    public Identifier getId() {
        return this.block.getId();
    }
}
