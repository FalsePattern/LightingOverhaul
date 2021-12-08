package com.lightingoverhaul.coremod.util;

import java.util.Stack;

public final class LightBuffer {
    private static final ThreadLocal<Stack<Light>> lights = ThreadLocal.withInitial(Stack::new);
    private static final ThreadLocal<Stack<RGB>> rgbs = ThreadLocal.withInitial(Stack::new);

    public static Light getLight() {
        Stack<Light> stack = lights.get();
        if (stack.isEmpty()) return new Light() {
            @Override
            public void close() {
                release(this);
            }
        };
        return stack.pop();
    }

    public static RGB getRGB() {
        Stack<RGB> stack = rgbs.get();
        if (stack.isEmpty()) return new RGB() {
            @Override
            public void close() {
                release(this);
            }
        };
        return stack.pop();
    }

    private static void release(Light light) {
        lights.get().push(light);
    }

    private static void release(RGB light) {
        rgbs.get().push(light);
    }
}
