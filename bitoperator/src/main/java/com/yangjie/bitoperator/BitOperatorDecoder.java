package com.yangjie.bitoperator;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.codec.BooleanCodec;
import com.yangjie.bitoperator.codec.Codec;
import com.yangjie.bitoperator.codec.CodecManager;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthType;
import com.yangjie.bitoperator.enums.LengthUnit;
import com.yangjie.bitoperator.metadata.FieldWrapper;
import com.yangjie.bitoperator.test.RtpHeader;
import com.yangjie.bitoperator.utils.BeanUtils;
import com.yangjie.bitoperator.utils.BitsExtractor;
import com.yangjie.bitoperator.utils.FieldUtils;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants.*;

public class BitOperatorDecoder {

    private byte[] src;

    private int startIndex;

    private Class<?> target;

    private Class<?> genericType;

    private static CodecManager codecManager = CodecManager.getInstance();

    public BitOperatorDecoder startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public BitOperatorDecoder src(byte[] src) {
        this.src = src;
        return this;
    }

    public BitOperatorDecoder target(Class<?> target) {
        this.target = target;
        return this;
    }

    public BitOperatorDecoder genericType(Class<?> genericType) {
        this.genericType = genericType;
        return this;
    }

    private Object read(byte[] src, Class<?> target) throws IllegalAccessException {
        //1.获取注解
        List<FieldWrapper> fields = BeanUtils.getFields(target);
        Object obj = BeanUtils.newBean(target);
        if (obj == null) {
            throw new NullPointerException("obj");
        }
        long lastNumberValue = 0;
        for (FieldWrapper fieldWrapper :
                fields) {
            Field field = fieldWrapper.getField();
            BitsProperty annotation = field.getAnnotation(BitsProperty.class);

            DataFormat dataFormat = annotation.dataFormat();
            fieldWrapper.setFormat(dataFormat);
            LengthType lengthType = annotation.lengthType();
            int length = annotation.length();
            LengthUnit lengthUnit = annotation.unit();

            Class<? extends Codec> codecCls = annotation.codec();
            Codec codec = codecManager.getCodec(codecCls);

            if (LengthType.PRE == lengthType) {
                length = (int) (lastNumberValue * BYTE_LENGTH);
            } else if (LengthUnit.BYTE == lengthUnit) {
                length = length * BYTE_LENGTH;
            }

            if (DataFormat.STRING == dataFormat) {
                int byteLength = (length - 1) / 8 + 1;
                int byteIndex = (startIndex) / 8;
                String str = new String(src, byteIndex, byteLength);
                FieldUtils.setObject(obj, field, str);

                length = str.getBytes().length * BYTE_LENGTH;
            } else if (DataFormat.BEAN == dataFormat) {
                if (fieldWrapper.isHasGenericType()) {
                    if (genericType == null) {
                        throw new NullPointerException("generic type cant't null");
                    }
                    fieldWrapper.setParameterizedType(genericType);
                }
                Class<?> parameterizedTypeClass = fieldWrapper.getParameterizedType();
                Object fieldObj = read(src, parameterizedTypeClass);
                FieldUtils.setObject(obj, field, fieldObj);
                length = 0;
            } else if (DataFormat.LIST == dataFormat) {
                length = annotation.length();
                if (LengthType.PRE == lengthType) {
                    length = (int) lastNumberValue;
                }
                for (int i = 0; i < length; i++) {
                    Class<?> parameterizedTypeClass = fieldWrapper.getParameterizedType();
                    Object fieldObj = read(src, parameterizedTypeClass);
                    Object listObj = field.get(obj);
                    if (listObj == null) {
                        listObj = new ArrayList<>(length + 1);
                        FieldUtils.setObject(obj, field, listObj);
                    }
                    List<Object> list = (List<Object>) listObj;
                    list.add(fieldObj);
                }

                length = 0;
            } else if (DataFormat.BOOLEAN == dataFormat) {
                if (codec == null) {
                    codec = codecManager.getCodec(BooleanCodec.class);
                }
                codec.decode(src, src.length, startIndex, length, obj, field, annotation);
            } else {
                long value = BitsExtractor.fromArray(src, startIndex, length);
                codec.decode(src, src.length, startIndex, length, obj, field, annotation);
                lastNumberValue = value & 0xffffffffffffffffL;
            }
            startIndex += length;
        }
        return obj;
    }

    public Object doDecode() {
        try {
            return read(src, target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        byte[] src = new byte[]{
                0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x6a, 0x61, 0x76, 0x61, 0x62, 0x65, 0x61, 0x6e,
                0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x6a, 0x61, 0x76, 0x61, 0x62, 0x65, 0x61, 0x6e,
                0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x6a, 0x61, 0x76, 0x61, 0x62, 0x65, 0x61, 0x6e,
                0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x6a, 0x61, 0x76, 0x61, 0x62, 0x65, 0x61, 0x6e,
                (byte) 0x80, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0b, (byte) 0xeb, (byte) 0xc2, 0x63,
                0xb, 0x62, 0x69, 0x74, 0x6f, 0x70, 0x65, 0x72, 0x61, 0x74, 0x6f, 0x72,
        };
        int length = src.length;
        System.out.println("length == " + length);
        RtpHeader o = (RtpHeader) BitOperator.decode(src, RtpHeader.class).doDecode();
        System.out.println(o);
    }
}
