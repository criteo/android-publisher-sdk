package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;

public class CriteoInterstitialListenerCallTask extends AsyncTask<Object, Void, Object> {
    private static final String TAG = "Criteo.ILCT";

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    public CriteoInterstitialListenerCallTask(CriteoInterstitialAdListener listener) {
        this.criteoInterstitialAdListener = listener;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Object result = null;

        try {
            result = doInterstitialListenerCallTask(objects);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal ILCT exec error.", tr);
        }

        return result;
    }

    private Object doInterstitialListenerCallTask(Object... objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }
        Object object = objects[0];
        return object;
    }

    @Override
    protected void onPostExecute(Object object) {
        try {
            doOnPostExecute(object);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal ILCT PostExec error.", tr);
        }
    }

    private void doOnPostExecute(Object object) {
        super.onPostExecute(object);
        if(criteoInterstitialAdListener != null) {
            if (object == null) {
                criteoInterstitialAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                return;
            }

            if (object instanceof Slot) {
                Slot slot = (Slot) object;
                if (!slot.isValid() || !URLUtil
                        .isValidUrl(slot.getDisplayUrl())) {
                    criteoInterstitialAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                }
            } else if (object instanceof TokenValue) {
                TokenValue tokenValue = (TokenValue) object;
                if (tokenValue == null) {
                    criteoInterstitialAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                }
            }
        }
    }
}
