package com.lightingoverhaul.coremod.util.operators;

import com.lightingoverhaul.coremod.util.Holder;

public interface UnaryOperator {
    int apply(int a);

    default <T extends Holder<T>> void apply(Holder<T> out, Holder<T> in) {
        int size = out.size();
        if (in.size() != size) throw new IllegalArgumentException("Operand size mismatch!");
        for (int i = 0; i < size; i++) {
            out.set(i, apply(in.get(i)));
        }
    }
}
