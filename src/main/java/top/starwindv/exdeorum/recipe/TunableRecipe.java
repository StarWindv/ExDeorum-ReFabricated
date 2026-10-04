/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.recipe;

import net.minecraft.world.item.crafting.Recipe;

/**
 * Implemented by recipes whose drop chance can be retuned from
 * {@code config/exdeorum-probabilities.json}.
 * <p>
 * Nothing here is keyed on a specific item: the probability config screen enumerates
 * whatever recipes implement this interface, so adding a new tunable recipe only means
 * implementing the interface (or extending {@link ProbabilityRecipe}), never editing a
 * list of ids.
 */
public interface TunableRecipe {
    /**
     * @return the chance a single roll succeeds, in the range {@code [0, 1]}
     */
    float tunableProbability();

    /**
     * @return how many independent rolls are made per use; {@code 1} for a flat chance
     */
    int tunableRolls();

    /**
     * @return {@code true} if this recipe's probability is a plain constant the player may
     * change freely. Recipes built from a uniform or computed provider are read only.
     */
    default boolean tunableEditable() {
        return true;
    }

    /**
     * @return a copy of this recipe with its success chance replaced
     */
    Recipe<?> withTunableProbability(float probability);
}