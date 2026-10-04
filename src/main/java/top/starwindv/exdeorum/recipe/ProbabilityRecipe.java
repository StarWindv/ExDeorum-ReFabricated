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

package top.starwindv.exdeorum.recipe;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public abstract class ProbabilityRecipe extends SingleIngredientRecipe implements TunableRecipe {
    public final ItemStackTemplate result;
    public final NumberProvider resultAmount;

    public ProbabilityRecipe(Ingredient ingredient, ItemStackTemplate result, NumberProvider resultAmount) {
        super(ingredient);
        this.result = result;
        this.resultAmount = resultAmount;
    }

    protected static <T extends ProbabilityRecipe> Products.P3<RecordCodecBuilder.Mu<T>, Ingredient, ItemStackTemplate, NumberProvider> commonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                CodecUtil.ingredientField(),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(ProbabilityRecipe::result),
                NumberProviders.CODEC.fieldOf("result_amount").forGetter(ProbabilityRecipe::resultAmount)
        );
    }

    public ItemStackTemplate result() {
        return this.result;
    }

    public NumberProvider resultAmount() {
        return this.resultAmount;
    }

    // Most recipes are a binomial draw: "n rolls, each succeeding with probability p". Some
    // are a plain constant, which behaves the same as a binomial with n = 1.
    private BinomialDistributionGenerator asBinomial() {
        if (this.resultAmount instanceof BinomialDistributionGenerator binomial
                && binomial.n() instanceof ConstantValue n
                && binomial.p() instanceof ConstantValue p) {
            return binomial;
        }

        if (this.resultAmount instanceof ConstantValue constant) {
            return BinomialDistributionGenerator.binomial(1, constant.value());
        }

        return null;
    }

    @Override
    public float tunableProbability() {
        var binomial = asBinomial();
        return binomial == null ? 0.0f : ((ConstantValue) binomial.p()).value();
    }

    @Override
    public int tunableRolls() {
        var binomial = asBinomial();
        return binomial == null ? 1 : Math.round(((ConstantValue) binomial.n()).value());
    }

    @Override
    public boolean tunableEditable() {
        return asBinomial() != null;
    }

    /** Rebuilds a recipe's result amount with a new success chance, preserving its roll count. */
    public static NumberProvider withProbability(ProbabilityRecipe recipe, float probability) {
        return BinomialDistributionGenerator.binomial(recipe.tunableRolls(), Mth.clamp(probability, 0.0f, 1.0f));
    }
}
