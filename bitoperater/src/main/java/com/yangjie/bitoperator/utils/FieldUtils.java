package com.yangjie.bitoperator.utils;

import java.lang.reflect.Field;

public class FieldUtils {

    /*public static void set(Field field, long value, Object object) throws IllegalAccessException {
        Field field = field;
        String typeName = field.getType().getName();
        value = value & 0xffffffffffffffffL;
        switch (typeName) {
            case "java.lang.Integer":
            case "int":
                setInt(fieldWrapper, (int) value, object);
                break;
            case "jva.lang.Boolean":
            case "boolean":
                break;
            case "java.lang.Long":
            case "long":
                setLong(fieldWrapper, value, object);
                break;
            case "java.lang.Byte":
            case "byte":
                setByte(fieldWrapper, (byte) value, object);
                break;
            case "java.lang.Short":
            case "short":
                setShort(fieldWrapper, (short) value, object);
                break;
        }
    }
*/
    public static void setLong(Object obj,Field field, long value,  boolean hasBasicType) throws IllegalAccessException {
        if (hasBasicType) {
            setObject(obj,field, new Long(value));
        } else {
            field.setLong(obj, value);
        }
    }

    public static void setByte(Object obj,Field field, byte value,  boolean hasBasicType) throws IllegalAccessException {
        if (hasBasicType) {
            setObject(obj,field, new Byte(value));
        } else {
            field.setByte(obj, value);
        }
    }

    public static void setShort(Object obj,Field field, short value,  boolean hasBasicType) throws IllegalAccessException {
        if (hasBasicType) {
            setObject(obj,field, new Short(value));
        } else {
            field.setShort(obj, value);
        }
    }

    public static void setInt( Object obj, Field field, int value,boolean hasBasicType) throws IllegalAccessException {
        if (hasBasicType) {
            field.setInt(obj, value);
        } else {
            setObject(obj,field, new Integer(value));

        }
    }


    public static void setBoolean( Object obj,Field field, boolean value) throws IllegalAccessException {
        if ("boolean".equals(field.getName())) {
            field.setBoolean(obj, value);
        } else {
            setObject(obj,field, value);
        }
    }

    public static void setBoolean(Object obj,Field field, boolean value,  boolean hasBasicType) throws IllegalAccessException {
        if (hasBasicType) {
            field.setBoolean(obj, value);
        } else {
            setObject(obj,field, value);
        }
    }

    public static void setObject(Object obj, Field field, Object value) throws IllegalAccessException {
        field.set(obj,value);
    }
}
