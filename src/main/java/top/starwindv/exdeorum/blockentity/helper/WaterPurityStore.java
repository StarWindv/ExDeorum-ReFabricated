/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.blockentity.helper;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import top.starwindv.exdeorum.compat.thirst.ThirstCompat;

/**
 * The water purity of a tank that only ever holds one fluid, persisted alongside it.
 *
 * <p>Everything here is a no-op unless Thirst Was Taken is loaded — without it water has no
 * purity at all, not in memory, not in saved data, not on items.
 *
 * <p>Additions mix downwards — topping up clean water with dirty water leaves dirty water.
 * Assigning replaces outright and is for refills from empty, so a stale value cannot leak into
 * a tank that was drained dry first.
 */
public final class WaterPurityStore {
    private static final String TAG = "waterPurity";

    private int purity = ThirstCompat.defaultPurity();

    public void assign(int purity) {
        if (ThirstCompat.loaded()) {
            this.purity = purity;
        }
    }

    public void mix(int purity) {
        if (ThirstCompat.loaded()) {
            this.purity = Math.min(this.purity, purity);
        }
    }

    public int get() {
        return this.purity;
    }

    public void load(ValueInput input) {
        if (ThirstCompat.loaded()) {
            this.purity = input.getIntOr(TAG, ThirstCompat.defaultPurity());
        }
    }

    public void save(ValueOutput output) {
        if (ThirstCompat.loaded()) {
            output.putInt(TAG, this.purity);
        }
    }
}
