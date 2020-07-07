package com.yangjie.bitoperator;

import java.nio.ByteBuffer;

public class BitOperatorFactory {

    public static BitOperatorDecoder decode(ByteBuffer byteBuffer, Class<?> target) {
        return decode(byteBuffer.array(), target);
    }

    public static BitOperatorDecoder decode(byte[] src, Class<?> target) {
        if (src == null) {
            throw new NullPointerException("src");
        }
        if (target == null) {
            throw new NullPointerException("target");
        }
        BitOperatorDecoder bitOperatorReader = new BitOperatorDecoder();
        bitOperatorReader.src(src);
        bitOperatorReader.target(target);
        return bitOperatorReader;
    }


    public static BitOperatorEncoder encode(Object src) {
        if (src == null) {
            throw new NullPointerException("src");
        }
        BitOperatorEncoder bitOperatorWriter = new BitOperatorEncoder();
        bitOperatorWriter.src(src);
        return bitOperatorWriter;
    }
}
