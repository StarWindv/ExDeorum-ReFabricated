/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import top.starwindv.exdeorum.compat.ModIds;

/**
 * Rustic Delight seeds.
 *
 * <p>{@code coffee_beans} is the odd one out: it has no "seed" in its name, so it is only
 * discoverable through the {@code c:seeds/coffee_beans} tag. The {@code *_seeds_bag} items are
 * storage crates and are deliberately not touched.
 */
record RusticDelightSeeds() implements ModSeeds {
    @Override
    public String modId() {
        return ModIds.RUSTIC_DELIGHT;
    }

    @Override
    public void register(Collector collector) {
        collector.seedFromDirt(modId(), "bell_pepper_seeds");
        collector.seedFromDirt(modId(), "pale_bell_pepper_seeds");
        collector.seedFromDirt(modId(), "dark_bell_pepper_seeds");
        collector.seedFromDirt(modId(), "cotton_seeds");
        collector.seedFromDirt(modId(), "coffee_beans");
    }
}