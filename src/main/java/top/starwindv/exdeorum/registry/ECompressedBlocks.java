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

package top.starwindv.exdeorum.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import top.starwindv.exdeorum.registry.DeferredBlock;
import top.starwindv.exdeorum.block.CompressedBlockType;

import java.util.ArrayList;
import java.util.List;

public class ECompressedBlocks {
    public static final List<CompressedBlockType> ALL_VARIANTS = new ArrayList<>();

    public static final CompressedBlockType COMPRESSED_DIRT = register(Blocks.DIRT).withCompressium();
    public static final CompressedBlockType COMPRESSED_COBBLESTONE = register(Blocks.COBBLESTONE).withCompressium();
    public static final CompressedBlockType COMPRESSED_DIORITE = register(Blocks.DIORITE).withCompressium();
    public static final CompressedBlockType COMPRESSED_GRANITE = register(Blocks.GRANITE).withCompressium();
    public static final CompressedBlockType COMPRESSED_ANDESITE = register(Blocks.ANDESITE).withCompressium();
    public static final CompressedBlockType COMPRESSED_GRAVEL = register(Blocks.GRAVEL).withCompressium();
    public static final CompressedBlockType COMPRESSED_SAND = register(Blocks.SAND).withCompressium();
    public static final CompressedBlockType COMPRESSED_DUST = register(EBlocks.DUST);
    public static final CompressedBlockType COMPRESSED_RED_SAND = register(Blocks.RED_SAND).withCompressium();
    public static final CompressedBlockType COMPRESSED_DEEPSLATE = register(Blocks.DEEPSLATE);
    public static final CompressedBlockType COMPRESSED_COBBLED_DEEPSLATE = register(Blocks.COBBLED_DEEPSLATE);
    public static final CompressedBlockType COMPRESSED_NETHERRACK = register(Blocks.NETHERRACK).withCompressium();
    public static final CompressedBlockType COMPRESSED_BLACKSTONE = register(Blocks.BLACKSTONE);
    public static final CompressedBlockType COMPRESSED_END_STONE = register(Blocks.END_STONE);
    public static final CompressedBlockType COMPRESSED_CRUSHED_DEEPSLATE = register(EBlocks.CRUSHED_DEEPSLATE);
    public static final CompressedBlockType COMPRESSED_CRUSHED_BLACKSTONE = register(EBlocks.CRUSHED_BLACKSTONE);
    public static final CompressedBlockType COMPRESSED_CRUSHED_NETHERRACK = register(EBlocks.CRUSHED_NETHERRACK);
    public static final CompressedBlockType COMPRESSED_SOUL_SAND = register(Blocks.SOUL_SAND).withCompressium();
    public static final CompressedBlockType COMPRESSED_CRUSHED_END_STONE = register(EBlocks.CRUSHED_END_STONE);
    public static final CompressedBlockType COMPRESSED_MOSS_BLOCK = register(Blocks.MOSS_BLOCK);

    private static CompressedBlockType register(Block vanillaBase) {
        CompressedBlockType type = new CompressedBlockType(BuiltInRegistries.BLOCK.getKey(vanillaBase).getPath(), () -> vanillaBase);
        ALL_VARIANTS.add(type);
        return type;
    }

    private static CompressedBlockType register(DeferredBlock<Block> base) {
        CompressedBlockType type = new CompressedBlockType(base.getId().getPath(), base);
        ALL_VARIANTS.add(type);
        return type;
    }

    public static void register() {
    }
}
