package com.yangjie.bitoperator;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.codec.Codec;
import com.yangjie.bitoperator.codec.CodecManager;
import com.yangjie.bitoperator.constants.BasicsTypeDataLengthConstants;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthUnit;
import com.yangjie.bitoperator.exceptions.OrderException;
import com.yangjie.bitoperator.metadata.FieldWrapper;
import com.yangjie.bitoperator.test.RtpHeader2;
import com.yangjie.bitoperator.utils.BeanUtils;
import com.yangjie.bitoperator.utils.HexStringUtils;
import com.yangjie.bitoperator.utils.SizeUtils;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;

public class BitOperatorEncoder {


    private int startIndex;

    private int endIndex;

    private Object src;

    private CodecManager codecManager = CodecManager.getInstance();

    private ByteBuffer buffer;

    private boolean hasIntroduceBuffer;

    public static int DEFAULT_BYTE_BUFFER_SIZE = 1500;

    public int bufferSize = DEFAULT_BYTE_BUFFER_SIZE;

    public BitOperatorEncoder src(Object src) {
        this.src = src;
        return this;
    }

    public BitOperatorEncoder startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public BitOperatorEncoder buffer(ByteBuffer byteBuffer) {
        this.buffer = byteBuffer;
        this.hasIntroduceBuffer = true;
        return this;
    }

    public BitOperatorEncoder bufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    private int write(Object obj, byte[] target, int bitIndex) throws IllegalAccessException, OrderException {
        Class<?> aClass = obj.getClass();
        List<FieldWrapper> fields = BeanUtils.getFields(aClass);
        int currentStartIndex = bitIndex;
        for (FieldWrapper fieldWrapper :
                fields) {
            Field field = fieldWrapper.getField();
            BitsProperty annotation = field.getAnnotation(BitsProperty.class);
            DataFormat dataFormat = annotation.dataFormat();
            int length = annotation.length();
            LengthUnit lengthUnit = annotation.unit();
            int index = annotation.index();
            if (LengthUnit.BYTE == lengthUnit) {
                length = length * BasicsTypeDataLengthConstants.BYTE_LENGTH;
            }
            if (index > 0) {
                int writeCount = bitIndex - currentStartIndex;
                if (writeCount > index) {
                    throw new OrderException("Indexes must be in increasing order");
                }

                bitIndex += (index - writeCount);
            }

            if (DataFormat.BEAN == dataFormat) {
                Object fieldBean = field.get(obj);
                /*
                 * 递归解析所有字段
                 */
                if (fieldBean != null) {
                    int writeNextIndex = write(fieldBean, target, bitIndex);
                    if (length <= 0) {
                        bitIndex = writeNextIndex;
                    }
                    //bitIndex = write(fieldBean, target, bitIndex);
                }
                if (length > 0) {
                    bitIndex += length;
                }
            } else if (DataFormat.LIST == dataFormat) {
                List<?> list = (List<?>) field.get(obj);
                /*
                 * 遍历+递归解析所有字段
                 */
                for (Object objItem :
                        list) {
                    bitIndex = write(objItem, target, bitIndex);
                }
            }
            /*
             * 基础类型和String写入数组
             */
            else {
                Class<? extends Codec> codec = annotation.codec();
                Codec codecObj = codecManager.getCodec(codec);
                bitIndex = codecObj.encode(target, 0, bitIndex, length, obj, field, annotation);
            }
        }

        return bitIndex;
    }

    public BitOperatorEncoder doEncode() {
        if (buffer == null) {
            buffer = ByteBuffer.allocate(bufferSize);
            hasIntroduceBuffer = false;
        }
        if (!hasIntroduceBuffer) {
            restBuffer();
        }

        try {
            int position = buffer.position();
            endIndex = write(src, buffer.array(), this.startIndex);
            buffer.position(position + SizeUtils.toByteSize(endIndex));
        } catch (IllegalAccessException | OrderException e) {
            e.printStackTrace();
        }
        return this;
    }

    private void restBuffer() {
        int position = buffer.position();
        byte[] array = buffer.array();
        while (position >= 0) {
            array[position] = 0;
            position--;
        }
        buffer.clear();
        endIndex = 0;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public byte[] toArray() {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        byte[] array = buffer.array();
        int length = endIndex == 0 ? 0 : (endIndex - 1) / 8 + 1;
        byte[] toArray = new byte[length];
        System.arraycopy(array, 0, toArray, 0, length);
        return toArray;
    }

    public static void main(String[] args) {
        RtpHeader2 rtpHeader2 = new RtpHeader2();
        byte[] bytes = BitOperator.encode(rtpHeader2).doEncode().toArray();
        System.out.println("hex = " + HexStringUtils.toHexString(bytes));
    }
}
