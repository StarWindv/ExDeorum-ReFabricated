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

package top.starwindv.exdeorum.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import top.starwindv.exdeorum.network.VisualUpdateTracker;
import top.starwindv.exdeorum.util.PendingBlockEntityLoads;

public abstract class EBlockEntity extends BlockEntity {
    public EBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // todo is this even necessary?
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Vanilla loads the received tag into the block entity itself, so NeoForge's
    // onDataPacket override is not needed here.

    // NeoForge's BlockEntity#onLoad is called once the block entity has a level and position.
    // Vanilla sets the level at exactly that point, but at that moment the block entity is
    // only being registered: its chunk is not finished loading and the block entity has not
    // been added to the level's ticker yet. Anything that reads neighbouring blocks, looks up
    // recipes or re-enters the light engine from here runs inside the chunk system and can
    // deadlock the world load, so the work is deferred until the block entity is actually
    // ticked, which vanilla only does once the chunk is fully loaded.
    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        if (level != null && this.level == level) {
            PendingBlockEntityLoads.add(this);
        }
    }

    // Called when this block entity is added to a level: either freshly placed or loaded from disk
    public void onLoad() {
    }

    public void markUpdated() {
        setChanged();
        VisualUpdateTracker.sendVisualUpdate(this);
    }

    public void writeVisualData(RegistryFriendlyByteBuf buffer) {
    }

    public void readVisualData(RegistryFriendlyByteBuf buffer) {
    }

    // Only called when data is sent by a local server
    public void copyVisualData(BlockEntity fromIntegratedServer) {
    }

    public InteractionResult useItemOn(Level level, Player player, ItemStack stack, InteractionHand hand) {
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    public InteractionResult useWithoutItem(Level level, Player player) {
        return InteractionResult.PASS;
    }
}
