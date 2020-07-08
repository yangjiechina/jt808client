package com.yangjie.bitoperator.metadata;

import com.yangjie.bitoperator.enums.DataFormat;

import java.lang.reflect.Field;

public class FieldWrapper {

    private Field field;


    private DataFormat format;


    private Class<?> parameterizedType;

    private boolean hasGenericType;

    public FieldWrapper(Field field) {
        this.field = field;
    }


    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public DataFormat getFormat() {
        return format;
    }

    public void setFormat(DataFormat format) {
        this.format = format;
    }

    public Class<?> getParameterizedType() {
        return parameterizedType;
    }

    public void setParameterizedType(Class<?> parameterizedType) {
        this.parameterizedType = parameterizedType;
    }

    public boolean isHasGenericType() {
        return hasGenericType;
    }

    public void setHasGenericType(boolean hasGenericType) {
        this.hasGenericType = hasGenericType;
    }
}
