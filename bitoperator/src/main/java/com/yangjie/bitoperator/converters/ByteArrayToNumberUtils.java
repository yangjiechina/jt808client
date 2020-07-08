package com.yangjie.bitoperator.converters;

import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.*;

public class ByteArrayToNumberUtils {

    public static short toShort(byte[] src, int startIndex) {
        return (short) to(src[startIndex], src[startIndex + 1]);
    }

    public static short toShort(byte src1, byte src2) {
        return (short) ((to(src1, src2)));
    }

    public static int toInt(byte[] src, int startIndex) {
        return (int) to(src[startIndex], src[startIndex + 1], src[startIndex + 2], src[startIndex + 3]);
    }

    public static int toInt(byte src1, byte src2, byte src3, byte src4) {
        return (int) to(src1, src2, src3, src4);

    }

    public static long toLong(byte[] src, int startIndex) {
        return to(src[startIndex], src[startIndex + 1], src[startIndex + 2], src[startIndex + 3], src[startIndex + 4], src[startIndex + 6], src[startIndex + 6], src[startIndex + 7]);

    }

    public static long toLong(byte src1, byte src2, byte src3, byte src4, byte src5, byte src6, byte src7, byte src8) {
        return to(src1, src2, src3, src4, src5, src6, src7, src8);
    }

    public static long to(byte[] src, int startIndex, int length) {
        byte[] temp = new byte[length];
        int count = 0;
        for (int i = startIndex; i < length; i++) {
            temp[count] = src[i];
            count++;
        }
        return to(temp);
    }

    private static long to(byte... bytes) {
        int length = bytes.length;
        if (length > LONG_LENGTH) {
            throw new IllegalArgumentException("length out of range!");
        }
        long temp = 0x0L;
        int offsetBits = (length - 1) * BYTE_LENGTH;
        for (int i = 0; i < length; i++) {
            offsetBits -= (i * BYTE_LENGTH);
            temp += ((bytes[i] & 0xff) << offsetBits);
        }
        return temp;
    }

    public static void main(String[] args) {
        byte b1 = (byte) 0xff;
        byte b2 = (byte) 0xff;
        byte b3 = (byte) 0xff;
        byte b4 = (byte) 0xff;
        byte b5 = (byte) 0xff;
        byte b6 = (byte) 0xff;
        byte b7 = (byte) 0xff;
        byte b8 = (byte) 0xff;
        short short1 = toShort(b1, b2);
        int to = (int) to(b1, b2);
        System.out.println("short 1 = " + (short1 & 0xffff));
        System.out.println("to = " + to);
        long l = toLong(b1, b2, b3, b4, b5, b6, b7, b8);
        System.out.println("l = " + (l & 0xffffffffffffffffL));

        long i = toInt(b1, b2, b3, b4);
        System.out.println("l = " + (i & 0xffffffffL));
    }

}
