package com.criteo.publisher.Util;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    private static final String GOOGLE_API_AVAILABILITY = "com.google.android.gms.common.GoogleApiAvailability";
    private static final String GOOGLE_ADVERTISING_ID_CLIENT = "com.google.android.gms.ads.identifier.AdvertisingIdClient";
    private static final String GET_ADVERTISING_ID_INFO = "getAdvertisingIdInfo";

    public static Class getClassFromString(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e("ReflectionUtil"
                    , "Failed to get Class: "+ className +" : " + e.getMessage());
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

    public static Object callGoogleApiAvailability(String methodName, Context context) {
        if (methodName == null || context == null) {
            return null;
        }

        try {
            Class<?> googleApiAvailability = getClassFromString(GOOGLE_API_AVAILABILITY);
            Method getInstance = googleApiAvailability.getMethod("getInstance");
            Object playServices = getInstance.invoke(googleApiAvailability);
            Method method = googleApiAvailability.getDeclaredMethod(methodName, Context.class);
            return method.invoke(playServices, context);
        } catch (Exception e) {
            Log.e("ReflectionUtil"
                    , "Failed to callGoogleApiAvailability method: "+ methodName
                            + " with context: "+ context.getClass().getName()
                            +" : " + e.getMessage());
        }
        return null;
    }

    public static Object callAdvertisingIdInfo(String methodName, Context context) {
        if (methodName == null || context == null) {
            return null;
        }

        try {
            Class<?> googleAdvertisingIdClient = getClassFromString(GOOGLE_ADVERTISING_ID_CLIENT);
            Method getAdvertisingIdInfo = googleAdvertisingIdClient.getMethod(GET_ADVERTISING_ID_INFO, Context.class);
            Object advertisingInfo = getAdvertisingIdInfo.invoke(googleAdvertisingIdClient, context);
            Method method = advertisingInfo.getClass().getDeclaredMethod(methodName);
            return method.invoke(advertisingInfo);
        } catch (Exception e) {
            Log.e("ReflectionUtil"
                    , "Failed to callAdvertisingIdClient method: "+ methodName
                            + " with context: "+ context.getClass().getName()
                            +" : " + e.getMessage());
        }
        return null;
    }
}
