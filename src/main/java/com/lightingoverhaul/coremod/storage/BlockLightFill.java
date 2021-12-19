package com.lightingoverhaul.coremod.storage;

public enum BlockLightFill {
    Zero, Mixed;
    public int toFlag() {
        switch (this) {
            default: return 0x00;
            case Zero: return 0x01;
            case Mixed: return 0x02;
        }
    }

    public static BlockLightFill fromFlag(int flag) {
        switch (flag & 0x0f) {
            default: throw new IllegalArgumentException("Flag " + flag + " is not a valid flag!");
            case 0x01: return Zero;
            case 0x02: return Mixed;
        }
    }
}
