package com.criteo.publisher.Util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    public static Class getClassFromString(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public static Object callMethodOnObject(Object object, String methodName, Object... params) {
        if (object == null || methodName == null || params == null) {
            return null;
        }

        try {
            int len = params.length;
            Class<?>[] classes = new Class[len];
            for (int i = 0; i < len; i++) {
                classes[i] = params[i].getClass();
            }
            Method method = object.getClass().getMethod(methodName, classes);
            return method.invoke(object, params);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
