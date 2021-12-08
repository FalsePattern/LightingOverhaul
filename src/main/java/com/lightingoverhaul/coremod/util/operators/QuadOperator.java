package com.lightingoverhaul.coremod.util.operators;

import com.lightingoverhaul.coremod.util.Holder;

public interface QuadOperator {
    int apply(int a, int b, int c, int d);

    default <T extends Holder<T>> void apply(Holder<T> out, Holder<T> a, Holder<T> b, Holder<T> c, Holder<T> d) {
        int size = out.size();
        if (a.size() != size || b.size() != size || c.size() != size || d.size() != size) throw new IllegalArgumentException("Operand size mismatch!");
        for (int i = 0; i < size; i++) {
            out.set(i, apply(a.get(i), b.get(i), c.get(i), d.get(i)));
        }
    }
}
