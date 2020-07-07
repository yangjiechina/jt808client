package com.yangjie.bitoperator.utils;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.exceptions.TypeNotSupportException;
import com.yangjie.bitoperator.metadata.FieldWrapper;

import java.lang.reflect.*;
import java.util.*;

public class BeanUtils {

    private static Map<String, Class<?>> cacheClassMap = new HashMap<>();
    private static Map<String, List<FieldWrapper>> cacheFieldsMap = new HashMap<>();

    private static Class<?> getClass(String className) throws ClassNotFoundException {
        Class<?> cls = cacheClassMap.get(className);
        if (cls == null) {
            cls = Class.forName(className);
            cacheClassMap.put(className, cls);
        }
        return cls;
    }

    public static List<FieldWrapper> getFields(String className) throws ClassNotFoundException, TypeNotSupportException {
        return getFields(getClass(className));
    }

    public static List<FieldWrapper> getFields(Class<?> cls) {
        String className = cls.getSimpleName();
        List<FieldWrapper> fields = cacheFieldsMap.get(className);
        if (fields == null) {
            try {
                fields = processField(getAllFields(cls));
            } catch (TypeNotSupportException e) {
                e.printStackTrace();
            }
            cacheFieldsMap.put(className, fields);
        }
        if (fields == null) {
            throw new NullPointerException("fields");
        }
        return fields;
    }

    /**
     * get all fields containing parent class
     * source of https://blog.csdn.net/qq_40406929/article/details/86217642
     *
     * @param cls class
     * @return field array
     */
    public static Field[] getAllFields(Class<?> cls) {
        List<Field> fieldList = new LinkedList<>();
        Class<?> clazz = cls;
        while (clazz != null) {
            fieldList.addAll(0, new LinkedList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = getSuperClass(clazz);
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    public static Method findMethod(Method[] methods, String methodName) {
        Method findMethod = null;
        for (Method method :
                methods) {
            if (method.getName().equals(methodName)) {
                findMethod = method;
                break;
            }
        }
        return findMethod;
    }

    public static Method getMethod(Class<?> cls, String methodName) {
        Method[] methods = cls.getMethods();
        Method findMethod = findMethod(methods, methodName);
        return findMethod != null ? findMethod : getDeclaredMethod(cls, methodName);

    }

    public static Method getDeclaredMethod(Class<?> cls, String methodName) {
        Method[] methods = cls.getDeclaredMethods();
        return findMethod(methods, methodName);
    }

    public static Object invoke(Object object, Method method, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Class<?> getSuperClass(Class<?> cls) {
        return cls.getSuperclass();
    }

    private static List<FieldWrapper> processField(Field[] fields) throws TypeNotSupportException {
        List<FieldWrapper> cacheList = new LinkedList<>();
        for (Field field :
                fields) {
            field.setAccessible(true);
            if (!field.isAnnotationPresent(BitsProperty.class)) {
                continue;
            }
            BitsProperty annotation = field.getAnnotation(BitsProperty.class);
            DataFormat dataFormat = annotation.dataFormat();
            String typeName = field.getType().getName();
            String name = field.getName();

            FieldWrapper fieldWrapper = new FieldWrapper(field);

            if (DataFormat.STRING == dataFormat) {
                if (!String.class.getName().equals(typeName)) {
                    throw new ClassCastException(name + "field type error , the target type is String , but the source type is " + typeName);
                }
            } else if (DataFormat.BEAN == dataFormat) {
                Class<?> type = field.getType();
                Type genericType = field.getGenericType();
                fieldWrapper.setParameterizedType(type);
                fieldWrapper.setHasGenericType("T".equals(genericType.getTypeName()));
            } else if (DataFormat.LIST == dataFormat) {
                Class<?> type = field.getType();
                Class<?>[] interfaces = type.getInterfaces();
                boolean isList = false;
                String listClassName = List.class.getName();
                String collectionClassName = Collection.class.getName();
                for (Class<?> cls :
                        interfaces) {
                    String clsName = cls.getName();
                    isList = clsName.equals(listClassName) || clsName.equals(collectionClassName);
                    if (isList) {
                        break;
                    }
                }
                if (!isList) {
                    throw new ClassCastException(name + "field type error , the target type is list , but the source type is " + typeName);
                }
                //get parameterized type
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                Class<?> parameterizedTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                fieldWrapper.setParameterizedType(parameterizedTypeClass);

            } else if (DataFormat.BOOLEAN == dataFormat) {
                long equals = annotation.equals();
                if (equals == -1) {
                    throw new NullPointerException("equals");
                }
            }
            cacheList.add(fieldWrapper);
        }
        return cacheList;
    }

    public static Object newBean(Class<?> cls) {
        Object o = null;
        try {
            o = cls.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }
}
