package com.yangjie.jt808.codec;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.codec.CommonCodec;
import com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants;
import com.yangjie.bitoperator.utils.FieldUtils;
import com.yangjie.bitoperator.utils.SizeUtils;

import java.lang.reflect.Field;

public class RegisterReplyCodec extends CommonCodec {

    @Override
    public void decode(byte[] array, int dataLength, int bitIndex, int length, Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException {
        int index = SizeUtils.toByteSize(bitIndex);
        if (index < dataLength) {
            String code = new String(array, index, dataLength - index-1);
            FieldUtils.setObject(obj,field,code);
            bitIndex += (code.getBytes().length* BasicsTypeDataLengthConstants.BYTE_LENGTH);
        }
    }
}
