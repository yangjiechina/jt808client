package com.yangjie.bitoperator.codec;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.utils.BitsExtractor;
import com.yangjie.bitoperator.utils.FieldUtils;

import java.lang.reflect.Field;

public class BooleanCodec extends CommonCodec {

    @Override
    public int encode(byte[] array, int dataLength, int bitIndex, int length, Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException {
        return super.encode(array, dataLength, bitIndex, length, obj, field, bitsProperty);
    }

    @Override
    public void decode(byte[] array,  int dataLength,int bitStartIndex, int length, Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException {
        long right = bitsProperty.equals();
        long left = BitsExtractor.fromArray(array, bitStartIndex, length);
        FieldUtils.setBoolean(obj,field, (right & 0xffffffffffffffffL) == (left & 0xffffffffffffffffL));
    }
}
