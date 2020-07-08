package com.yangjie.bitoperator.converters;

public class NumberToByteArrayUtils {

    public static byte[] byByte(byte src) {
        return by(src);
    }

    public static byte[] byShort(short src) {
        return by((byte) (src >> 8), (byte) (src & 0x00ff));
    }

    public static byte[] byInt(int src) {
        return by((byte) (src >> 24), (byte) (src >> 16), (byte) (src >> 8), (byte) (src & 0x000000ff));
    }

    public static byte[] byLong(long src) {
        return by((byte) (src >> 56), (byte) (src >> 48), (byte) (src >> 40), (byte) (src >> 32), (byte) (src >> 24), (byte) (src >> 16), (byte) (src >> 8), (byte) (src & 0x00000000000000ff));
    }

    private static byte[] by(byte... bytes) {
        int length = bytes.length;
        byte[] result = new byte[length];
        System.arraycopy(bytes, 0, result, 0, length);
        return result;
    }

    public static void main(String[] args) {
        short s1 = (short) 0xffff;
        byte[] bytes = byShort(s1);
        System.out.println("b1 == " + (bytes[0] & 0xff) + "b2 == " + (bytes[1] & 0xff));
    }

}
