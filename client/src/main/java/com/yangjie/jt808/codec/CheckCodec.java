package com.yangjie.jt808.codec;


import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.codec.CommonCodec;
import com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants;
import com.yangjie.bitoperator.write.ArrayWriter;

import java.lang.reflect.Field;

public class CheckCodec extends CommonCodec {

    @Override
    public int encode(byte[] array, int dataLength, int bitIndex, int length, Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException {
        byte code = 0;
        int index = bitIndex / BasicsTypeDataLengthConstants.BYTE_LENGTH;
        for (int i = 0; i < index; i++) {
            code ^= array[i];
        }
        ArrayWriter.write(array, bitIndex, code, length);
        return bitIndex + length;
    }
}
