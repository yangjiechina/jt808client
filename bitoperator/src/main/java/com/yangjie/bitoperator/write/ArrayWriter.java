package com.yangjie.bitoperator.write;

import com.yangjie.bitoperator.utils.BitsExtractor;

import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.*;

public class ArrayWriter {
    public static int write(byte[] src, int bitStartIndex, byte value, int length) {
        return w(src, bitStartIndex, length, value);
    }

    public static int write(byte[] src, int bitStartIndex, short value, int length) {
        return w(src, bitStartIndex, length, value);
    }

    public static int write(byte[] src, int bitStartIndex, int value, int length) {
        return w(src, bitStartIndex, length, value);
    }

    public static int write(byte[] src, int bitStartIndex, long value, int length) {
        return w(src, bitStartIndex, length, value);
    }

    public static int write(byte[] src, int index, byte[] value, int length) {
        System.arraycopy(value, 0, src, index, length);
        return (index * BYTE_LENGTH) + (length * BYTE_LENGTH);
    }

    private static int w(byte[] src, int bitStartIndex, int length, long value) {
        for (int i = length; i > 0; i--) {
            int arrayIndex = bitStartIndex / BYTE_LENGTH;
            int bitIndex = bitStartIndex % 8;
            byte writeBit = (byte) BitsExtractor.fromLong(value, (LONG_LENGTH - i), 1);
            src[arrayIndex] = BasicTypeWriter.write(src[arrayIndex], bitIndex, 1, writeBit);
            bitStartIndex++;
        }
        return bitStartIndex;
    }

    public static void main(String[] args) {
        byte[] src = new byte[2];
        write(src, 2, 2, (byte) 0x2);
        System.out.println(" index 0 = " + (src[0] & 0xff));
        System.out.println(" index 1 = " + (src[1] & 0xff));
        byte writeBit = (byte) BitsExtractor.fromLong(0x2, 6, 1);
        System.out.println(writeBit );
    }
}
