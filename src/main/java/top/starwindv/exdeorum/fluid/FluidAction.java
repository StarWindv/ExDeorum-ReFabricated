/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.fluid;

// Stand-in for NeoForge's FluidAction
public enum FluidAction {
    EXECUTE,
    SIMULATE;

    public boolean simulate() {
        return this == SIMULATE;
    }

    public boolean execute() {
        return this == EXECUTE;
    }
}
