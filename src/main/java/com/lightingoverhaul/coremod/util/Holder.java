package com.lightingoverhaul.coremod.util;

import com.lightingoverhaul.coremod.util.operators.BinaryOperator;
import com.lightingoverhaul.coremod.util.operators.QuadOperator;
import com.lightingoverhaul.coremod.util.operators.TernaryOperator;
import com.lightingoverhaul.coremod.util.operators.UnaryOperator;

public interface Holder<T extends Holder<T>> extends AutoCloseable {
    int size();
    int get(int index);
    void set(int index, int v);

    default Holder<T> apply(Holder<T> in, UnaryOperator operator) {
        operator.apply(this, in);
        return this;
    }

    default Holder<T> applySelf(UnaryOperator operator) {
        operator.apply(this, this);
        return this;
    }

    default Holder<T> apply(Holder<T> a, Holder<T> b, BinaryOperator operator) {
        operator.apply(this, a, b);
        return this;
    }

    default Holder<T> applySelf(Holder<T> b, BinaryOperator operator) {
        operator.apply(this, this, b);
        return this;
    }

    default Holder<T> apply(Holder<T> a, Holder<T> b, Holder<T> c, TernaryOperator operator) {
        operator.apply(this, a, b, c);
        return this;
    }

    default Holder<T> applySelf(Holder<T> b, Holder<T> c, TernaryOperator operator) {
        operator.apply(this, this, b, c);
        return this;
    }

    default Holder<T> apply(Holder<T> a, Holder<T> b, Holder<T> c, Holder<T> d, QuadOperator operator) {
        operator.apply(this, a, b, c, d);
        return this;
    }

    default Holder<T> applySelf(Holder<T> b, Holder<T> c, Holder<T> d, QuadOperator operator) {
        operator.apply(this, this, b, c, d);
        return this;
    }

    default int reduce(int initial, BinaryOperator accumulator) {
        int size = this.size();
        for (int i = 0; i < size; i++) {
            initial = accumulator.apply(initial, get(i));
        }
        return initial;
    }

    @Override
    default void close() throws RuntimeException {
        throw new UnsupportedOperationException();
    }
}
