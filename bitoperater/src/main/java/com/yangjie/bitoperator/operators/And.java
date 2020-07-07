package com.yangjie.bitoperator.operators;

import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.*;

public class And {

/*    public static byte execute(byte src, int startIndex, int length) {
        return execute(src,getAnd(startIndex, length, BYTE_LENGTH));
    }*/

    public static short to(short src, int startIndex, int length) {
        return (short) ((src & 0Xff) & (who(startIndex, length, SHORT_LENGTH) & 0xffff));
    }

    public static int to(int src, int startIndex, int length) {
        return (int) ((src & 0Xff) & (who(startIndex, length, INT_LENGTH) & 0xffffffffL));
    }

    public static long to(long src, int startIndex, int length) {
        return (long) ((src & 0Xff) & (who(startIndex, length, LONG_LENGTH) & 0xffffffffL));
    }

    public static byte to(byte src, byte right) {
        return (byte) ((src & 0xff) & right);
    }

    public static short to(short src, short right) {
        return (short) ((src & 0xffff) & right);
    }

    public static int to(int src, int right) {
        return (int) ((src & 0xffffffffL) & right);
    }

    public static long to(long src, long right) {
        return ((src & 0xffffffffffffffffL) & right);
    }

    public static long who(int startIndex, int length, int dataLength) {
        if (startIndex + length > dataLength) {
            throw new IllegalArgumentException(startIndex + " startIndex " + length + "length " + " length out of range ! ");
        }

        long result = 0x0L;
        int count = length;
        for (int i = 0; i < dataLength; i++) {
            if (i < startIndex) {
                continue;
            }
            if (count != 0) {
                result += 0x1;
                count--;
            }
            if (i != dataLength - 1) {
                result = result << 1;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        short and = (short) who(0, 4, 16);
        System.out.println(and & 0xffff);
        System.out.println((1 & 0xff) << 7);
    }

}
