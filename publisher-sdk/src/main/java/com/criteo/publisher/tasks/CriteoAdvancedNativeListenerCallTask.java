package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoNativeAd;
import com.criteo.publisher.CriteoNativeAdListener;
import com.criteo.publisher.model.Slot;

public class CriteoAdvancedNativeListenerCallTask extends AsyncTask<Object, Void, Object[]> {
    private static final String TAG = "Criteo.ANLCT";
    private CriteoNativeAdListener criteoNativeAdListener;

    public CriteoAdvancedNativeListenerCallTask(CriteoNativeAdListener criteoNativeAdListener) {
        this.criteoNativeAdListener = criteoNativeAdListener;
    }

    @Override
    protected Object[] doInBackground(Object... objects) {
        Object[] results = null;

        try {
            if (objects == null || objects.length < 2) {
                return null;
            }
            results = new Object[2];
            // Slot
            results[0] = objects[0];
            // CriteoNativeAd
            results[1] = objects[1];
        } catch (Throwable tr) {
            Log.e(TAG, "Internal ANLCT exec error.", tr);
        }
        return results;
    }

    @Override
    protected void onPostExecute(Object[] objects) {
        try {
            doOnPostExecute(objects);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal ANLCT PostExec error.", tr);
        }
    }

    private void doOnPostExecute(Object[] objects) {
        if (criteoNativeAdListener != null) {
            if (objects == null || objects.length < 2 || objects[0] == null || objects[1] == null) {
                criteoNativeAdListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
                return;
            }
            Slot slot = (Slot) objects[0];
            CriteoNativeAd nativeAd = (CriteoNativeAd) objects[1];
            if (!slot.isValid() || !slot.isNative()) {
                criteoNativeAdListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
            } else {
                criteoNativeAdListener.onAdReceived(nativeAd);
            }
        }
    }
}
