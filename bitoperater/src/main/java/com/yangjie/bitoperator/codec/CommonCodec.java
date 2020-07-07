package com.yangjie.bitoperator.codec;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.utils.BitsExtractor;
import com.yangjie.bitoperator.utils.FieldUtils;
import com.yangjie.bitoperator.write.ArrayWriter;

import java.lang.reflect.Field;

public class CommonCodec implements Codec {

    @Override
    public int encode(byte[] array,  int dataLength,int bitIndex, int length, Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException {
        String type = field.getType().getName();
        switch (type) {
            case "int":
                int anInt = field.getInt(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, anInt, length);
                break;
            case "java.lang.Integer":
                Integer integer = (Integer) field.get(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, integer.intValue(), length);
                break;
            case "byte":
                byte aByte = field.getByte(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, aByte, length);
                break;
            case "java.lang.Byte":
                Byte byteObj = (Byte) field.get(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, byteObj.byteValue(), length);
                break;
            case "short":
                short aShort = field.getShort(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, aShort, length);
                break;
            case "java.lang.Short":
                Short shortObj = (Short) field.get(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, shortObj.shortValue(), length);
                break;
            case "long":
                long aLong = field.getLong(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, aLong, length);
                break;
            case "java.lang.Long":
                Long longObj = (Long) field.get(obj);
                bitIndex = ArrayWriter.write(array, bitIndex, longObj.longValue(), length);
                break;
            case "java.lang.String":
                String str = (String) field.get(obj);
                byte[] strBytes = str.getBytes();
                bitIndex = ArrayWriter.write(array, bitIndex / 8, strBytes, strBytes.length);
                break;
            default:
                bitIndex+=length;
                break;
        }
        return bitIndex;
    }

    @Override
    public void decode(byte[] array,  int dataLength, int bitIndex,  int length,Object obj, Field field, BitsProperty bitsProperty) throws IllegalAccessException {
        String type = field.getType().getName();
        long value = BitsExtractor.fromArray(array, bitIndex, length);
        switch (type) {
            case "int":
                int anInt = (int) (value & 0xffffffffL);
                FieldUtils.setInt(obj,field,anInt,true);
                break;
            case "java.lang.Integer":
                int intObj = (int) (value & 0xffffffffL);
                FieldUtils.setObject(obj,field,intObj);
                break;
            case "byte":
                byte aByte = (byte) (value & 0xff);
                FieldUtils.setByte(obj,field,aByte,true);
            case "java.lang.Byte":
                byte byteObj = (byte) (value & 0xff);
                FieldUtils.setObject(obj,field,byteObj);
                break;
            case "short":
                short aShort = (short) (value & 0xffff);
                FieldUtils.setShort(obj,field,aShort,true);
                break;
            case "java.lang.Short":
                short shortObj = (short) (value & 0xffff);
                FieldUtils.setObject(obj,field,shortObj);
                break;
            case "long":
                FieldUtils.setLong(obj,field,value,true);
                break;
            case "java.lang.Long":
                FieldUtils.setObject(obj,field,value);
                break;
            default:
                break;
        }
    }
}
