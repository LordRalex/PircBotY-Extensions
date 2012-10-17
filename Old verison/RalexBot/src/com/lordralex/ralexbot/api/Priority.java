package com.lordralex.ralexbot.api;

/**
 *
 * @author Joshua
 */
public enum Priority {

    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4),
    FINAL(5);
    private final int slot;

    private Priority(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public static Priority[] getValues() {
        return new Priority[]{
                    LOWEST,
                    LOW,
                    NORMAL,
                    HIGH,
                    HIGHEST,
                    FINAL
                };
    }
}
