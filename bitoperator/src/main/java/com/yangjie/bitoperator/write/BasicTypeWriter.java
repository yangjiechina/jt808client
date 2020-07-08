package com.yangjie.bitoperator.write;

import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.*;

public class BasicTypeWriter {

    public static byte write(byte src, int startIndex, int length, byte value) {
        return (byte) write(src, startIndex, length, value, BYTE_LENGTH);
    }

    public static short write(short src, int startIndex, int length, short value) {
        return (short) write(src, startIndex, length, value, SHORT_LENGTH);
    }

    public static int write(int src, int startIndex, int length, int value) {
        return (int) write(src, startIndex, length, value, INT_LENGTH);
    }

    public static long write(long src, int startIndex, int length, long value) {
        return write(src, startIndex, length, value, LONG_LENGTH);
    }

    private static long write(long src, int startIndex, int length, long value, int dataLength) {
        int offsetShit = dataLength - (startIndex + length);
        return (src + (value << offsetShit));
    }

    public static void main(String[] args) {
        byte src = 0;

        src = write(src, 0, 1, (byte) 1);
        src = write(src, 1, 1, (byte) 1);
        src = write(src, 2, 1, (byte) 1);
        src = write(src, 3, 1, (byte) 1);
        src = write(src, 4, 1, (byte) 1);
        src = write(src, 5, 1, (byte) 1);
        src = write(src, 6, 1, (byte) 1);
        src = write(src, 7, 1, (byte) 1);
        System.out.println(src & 0xffffffffL);
    }
}
