package com.yangjie.bitoperator.utils;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthType;
import com.yangjie.bitoperator.enums.LengthUnit;
import com.yangjie.bitoperator.metadata.FieldWrapper;

import java.lang.reflect.Field;
import java.util.List;

import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.*;
import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.BYTE_LENGTH;

public class SizeUtils {

    public static int getObjBitSize(Object obj) {
        Class<?> aClass = obj.getClass();
        List<FieldWrapper> fields = BeanUtils.getFields(aClass);
        int bitCount = 0;
        try {
            for (FieldWrapper fieldWrapper :
                    fields) {
                Field field = fieldWrapper.getField();
                BitsProperty annotation = field.getAnnotation(BitsProperty.class);
                DataFormat format = annotation.dataFormat();
                LengthType lengthType = annotation.lengthType();
                int length = annotation.length();
                LengthUnit lengthUnit = annotation.unit();
                if(LengthUnit.BYTE == lengthUnit){
                    length = length * BYTE_LENGTH;
                }
                if (DataFormat.BEAN == format) {
                    Object o = field.get(obj);
                    if(length <= 0 && o != null){
                        length = getObjByteSize(o);
                    }
                } else if (DataFormat.STRING == format) {
                    String str = (String) field.get(obj);
                    length = !StringUtils.isEmpty(str) ? str.getBytes().length * BYTE_LENGTH : 0;
                } else if (DataFormat.LIST == format) {
                    List<?> objList = (List<?>) field.get(obj);
                    int size = objList != null ? objList.size() : 0;
                    if (size > 0) {
                        Object o = objList.get(0);
                        length = getObjBitSize(o) * size;
                    } else {
                        length = 0;
                    }
                }
                bitCount += length;
            }
        } catch (Exception e) {
            e.printStackTrace();
            bitCount = 0;
        }

        return bitCount;
    }

    public static int getObjByteSize(Object obj) {
        int bitSize = getObjBitSize(obj);
        return toByteSize(bitSize);
    }

    public static int toByteSize(int bitSize) {
        if (bitSize <= 0) {
            return 0;
        }
        return (bitSize - 1) / BYTE_LENGTH + 1;
    }

}
