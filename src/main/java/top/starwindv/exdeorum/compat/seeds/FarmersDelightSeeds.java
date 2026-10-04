/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import top.starwindv.exdeorum.compat.ModIds;

/**
 * Farmer's Delight seeds.
 *
 * <p>Only cabbage and tomato have their own seed items. Rice is planted with the vanilla
 * {@code minecraft:rice_seeds}, which Ex Deorum's own data pack does not sift, so it is covered
 * here; {@code farmersdelight:rice} is the grain, not a plantable seed.
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
        // Published into the vanilla namespace, resolved through the minecraft fallback
        collector.seedFromDirt(modId(), "rice_seeds");
    }
}