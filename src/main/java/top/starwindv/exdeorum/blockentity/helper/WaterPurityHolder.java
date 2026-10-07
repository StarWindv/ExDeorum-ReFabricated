/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.blockentity.helper;

/**
 * A block entity whose fluid tank tracks Thirst Was Taken's water purity.
 *
 * <p>Only meaningful for water tanks; every implementation is a no-op unless TWT-U is loaded.
 */
public interface WaterPurityHolder {
    /** Replaces the purity outright — for refills from empty, so no stale value lingers. */
    void setWaterPurity(int purity);

    /** Lowers the purity to at most the given value — for topping up existing water. */
    void mixWaterPurity(int purity);
}
