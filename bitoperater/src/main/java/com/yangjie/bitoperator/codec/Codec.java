package com.yangjie.bitoperator.codec;


import com.yangjie.bitoperator.annotations.BitsProperty;

import java.lang.reflect.Field;

public interface Codec {

    int encode(byte[] array, int dataLength, int bitIndex, int length, Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException;

    void decode(byte[] array, int dataLength, int bitIndex, int length, Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException;

}
