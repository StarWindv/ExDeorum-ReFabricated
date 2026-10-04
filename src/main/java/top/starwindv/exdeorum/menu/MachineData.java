/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.menu;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// Extended screen handler payload for the machine menus: the machine position plus redstone mode.
public record MachineData(BlockPos pos, byte redstoneMode) {
    public static final Codec<MachineData> CODEC = BlockPos.CODEC
            .xmap(pos -> new MachineData(pos, (byte) 0), MachineData::pos);

    public static final StreamCodec<RegistryFriendlyByteBuf, MachineData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MachineData::pos,
            ByteBufCodecs.BYTE, MachineData::redstoneMode,
            MachineData::new);
}
