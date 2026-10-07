/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.compat.seeds;

import top.starwindv.exdeorum.compat.ModIds;

/**
 * TechReborn drops.
 *
 * <p>A skyblock has no TechReborn ores or rubber trees, and TechReborn's own scrapbox lottery is
 * what hands out its unobtainable starters — nickel and tungsten dusts, rubber saplings, nuggets.
 * Every one of those chains therefore funnels down to a single item: scrap, nine of which craft
 * a scrap box. Sieving it from gravel lets the player bootstrap the lottery by hand, instead of
 * needing the recycler before any machine exists to power it.
 */
record TechRebornDrops() implements ModSeeds {
    @Override
    public String modId() {
        return ModIds.TECHREBORN;
    }

    @Override
    public void register(Collector collector) {
        collector.dropFromGravel(modId(), "scrap");
    }
}
