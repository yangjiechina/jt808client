package com.yangjie.bitoperator.utils;

import com.yangjie.bitoperator.operators.And;
import com.yangjie.bitoperator.write.BasicTypeWriter;

import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.*;

public class BitsExtractor {

    public static byte fromByte(byte src, int startIndex, int length) {
        return (byte) get(src, startIndex, length, BYTE_LENGTH);
    }

    public static short fromShort(short src, int startIndex, int length) {
        return (short) get(src, startIndex, length, SHORT_LENGTH);
    }

    public static int fromInt(int src, int startIndex, int length) {
        return (int) get(src, startIndex, length, INT_LENGTH);
    }

    public static long fromLong(long src, int startIndex, int length) {
        return get(src, startIndex, length, LONG_LENGTH);
    }

    private static long get(long src, int startIndex, int length, int dataLength) {
        if (startIndex + length > LONG_LENGTH) {
            throw new IllegalArgumentException("length out of range!");
        }
        int shiftCount = dataLength - (startIndex + length);
        return (src >> shiftCount) & And.who((dataLength - length), length, dataLength);
    }

    public static long fromArray(byte[] src, int startIndex, int length) {
        long result = 0;
        for (int i = length; i > 0; i--) {
            int arrayIndex = startIndex / BYTE_LENGTH;
            int bitIndex = startIndex % 8;
            byte writeBit = BitsExtractor.fromByte(src[arrayIndex], bitIndex, 1);
            result = BasicTypeWriter.write(result, (LONG_LENGTH-i), 1, writeBit);
            startIndex++;
        }
        return result;
    }


    public static void main(String[] args) {
        //byte src1 = (byte) 0xff;
        byte src1 = (byte) 0x2;

        short src2 = (short) 0xffff;

        int src3 = 0xffffffff;

        long src4 = 0xffffffffffffffffL;
        //1111 1111 1111 1111

        System.out.println("src 1 " + (fromByte(src1, 6, 2) & 0xff));
        System.out.println("src 2 " + (fromShort(src2, 1, 3) & 0xff));
        System.out.println("src 3 " + (fromInt(src3, 1, 3) & 0xff));
        System.out.println("src 4 " + (fromLong(src4, 1, 3) & 0xff));

        byte[] array = new byte[]{(byte) 0xff, (byte) 0xff};

        long arrayResult = fromArray(array, 7, 2 );
        System.out.println("arrayResult >>> "+(arrayResult & 0xff));

    }
}
