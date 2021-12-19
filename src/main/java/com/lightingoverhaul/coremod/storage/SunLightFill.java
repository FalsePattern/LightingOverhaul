package com.lightingoverhaul.coremod.storage;

public enum SunLightFill {
    Zero, Mixed, Full;

    public int toFlag() {
        switch (this) {
            default: return 0x00;
            case Zero: return 0x10;
            case Mixed: return 0x20;
            case Full: return 0x30;
        }
    }

    public static SunLightFill fromFlag(int flag) {
        switch (flag & 0xf0) {
            default: throw new IllegalArgumentException("Flag " + flag + " is not a valid flag!");
            case 0x10: return Zero;
            case 0x20: return Mixed;
            case 0x30: return Full;
        }
    }
}
