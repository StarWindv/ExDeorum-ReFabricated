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

package top.starwindv.exdeorum.recipe.crook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import top.starwindv.exdeorum.recipe.BlockPredicate;
import top.starwindv.exdeorum.recipe.TunableRecipe;
import top.starwindv.exdeorum.registry.ERecipeSerializers;
import top.starwindv.exdeorum.registry.ERecipeTypes;

public record CrookRecipe(BlockPredicate blockPredicate, ItemStackTemplate result, float chance) implements Recipe<RecipeInput>, TunableRecipe {
    public static final MapCodec<CrookRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPredicate.CODEC.fieldOf("block_predicate").forGetter(CrookRecipe::blockPredicate),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(CrookRecipe::result),
            Codec.FLOAT.fieldOf("chance").forGetter(CrookRecipe::chance)
    ).apply(instance, CrookRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrookRecipe> STREAM_CODEC = StreamCodec.of(CrookRecipe::toNetwork, CrookRecipe::fromNetwork);

    @Override
    public boolean matches(RecipeInput input, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public RecipeSerializer<CrookRecipe> getSerializer() {
        return ERecipeSerializers.CROOK.get();
    }

    @Override
    public RecipeType<CrookRecipe> getType() {
        return ERecipeTypes.CROOK.get();
    }

    @Override
    public float tunableProbability() {
        return this.chance;
    }

    @Override
    public int tunableRolls() {
        return 1;
    }

    @Override
    public Recipe<?> withTunableProbability(float probability) {
        return new CrookRecipe(this.blockPredicate, this.result, Mth.clamp(probability, 0.0f, 1.0f));
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, CrookRecipe recipe) {
        recipe.blockPredicate.toNetwork(buffer);
        ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
        buffer.writeFloat(recipe.chance);
    }

    public static CrookRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        BlockPredicate blockPredicate = BlockPredicate.STREAM_CODEC.decode(buffer);
        ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
        float chance = buffer.readFloat();

        return new CrookRecipe(blockPredicate, result, chance);
    }

}
