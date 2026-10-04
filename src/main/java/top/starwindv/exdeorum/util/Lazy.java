/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.util;

import java.util.function.Supplier;

// Stand-in for NeoForge's Lazy.
public final class Lazy<T> implements Supplier<T> {
    private Supplier<? extends T> supplier;
    private T value;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    @Override
    public T get() {
        if (this.supplier != null) {
            this.value = this.supplier.get();
            this.supplier = null;
        }
        return this.value;
    }
}
