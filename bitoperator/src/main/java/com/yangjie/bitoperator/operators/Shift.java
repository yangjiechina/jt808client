package com.yangjie.bitoperator.operators;

public class Shift {

    public static short execute(byte value, int length) {
        return execute(value, length, false);
    }

    public static short execute(byte value, int length, boolean hasRight) {
        return execute(value, length, false, false);
    }

    public static short execute(byte value, int length, boolean hasRight, boolean unsigned) {
        if (hasRight) {
            if (unsigned) {
                value = (byte) ((value & 0xff) >>> length);
            } else {
                value = (byte) ((value & 0xff) >> length);
            }
        } else {
            value = (byte) ((value & 0xff) << length);
        }
        return value;
    }

    public static short execute(short value, int length) {
        return execute(value, length, false);
    }

    public static short execute(short value, int length, boolean hasRight) {
        return execute(value, length, false, false);
    }

    public static short execute(short value, int length, boolean hasRight, boolean unsigned) {
        if (hasRight) {
            if (unsigned) {
                value = (short) ((value & 0xffff) >>> length);
            } else {
                value = (short) ((value & 0xffff) >> length);
            }
        } else {
            value = (short) ((value & 0xffff) << length);
        }
        return value;
    }

    public static int execute(int value, int length) {
        return execute(value, length, false);
    }

    public static int execute(int value, int length, boolean hasRight) {
        return execute(value, length, false, false);
    }

    public static int execute(int value, int length, boolean hasRight, boolean unsigned) {
        if (hasRight) {
            if (unsigned) {
                value = (int) ((value & 0xffffffffL) >>> length);
            } else {
                value = (int) ((value & 0xffffffffL) >> length);
            }
        } else {
            value = (int) ((value & 0xffffffffL) << length);
        }
        return value;
    }

    public static long execute(long value, int length) {
        return execute(value, length, false);
    }

    public static long execute(long value, int length, boolean hasRight) {
        return execute(value, length, false, false);
    }

    public static long execute(long value, int length, boolean hasRight, boolean unsigned) {
        if (hasRight) {
            if (unsigned) {
                value = ((value & 0xffffffffffffffffL) >>> length);
            } else {
                value =  ((value & 0xffffffffffffffffL) >> length);
            }
        } else {
            value = ((value & 0xffffffffffffffffL) << length);
        }
        return value;
    }
}
