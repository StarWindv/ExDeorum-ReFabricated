/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import top.starwindv.exdeorum.compat.ModIds;

/**
 * Ube's Delight seeds.
 *
 * <p>Ube, garlic and ginger are dual purpose: cutting a wild plant yields the crop, which is then
 * also the item you plant, the same pattern vanilla uses for wheat and carrots. Only lemongrass
 * has a dedicated seed item. All four end up in {@code minecraft:villager_plantable_seeds}.
 */
record UbesDelightSeeds() implements ModSeeds {
    @Override
    public String modId() {
        return ModIds.UBES_DELIGHT;
    }

    @Override
    public void register(Collector collector) {
        collector.seedFromDirt(modId(), "lemongrass_seeds");
        collector.seedFromDirt(modId(), "ube");
        collector.seedFromDirt(modId(), "garlic");
        collector.seedFromDirt(modId(), "ginger");
    }
}