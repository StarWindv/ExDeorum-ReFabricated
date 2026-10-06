/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import top.starwindv.exdeorum.compat.ModIds;

/**
 * Farmer's Delight seeds.
 *
 * <p>Only cabbage and tomato have their own seed items. Rice is planted with the grain item
 * {@code farmersdelight:rice} itself, the same pattern vanilla uses for carrots and potatoes,
 * so the grain is what gets sifted.
 */
record FarmersDelightSeeds() implements ModSeeds {
    @Override
    public String modId() {
        return ModIds.FARMERS_DELIGHT;
    }

    @Override
    public void register(Collector collector) {
        collector.seedFromDirt(modId(), "cabbage_seeds");
        collector.seedFromDirt(modId(), "tomato_seeds");
        // Planted directly as the grain, like vanilla carrots
        collector.seedFromDirt(modId(), "rice");
    }
}