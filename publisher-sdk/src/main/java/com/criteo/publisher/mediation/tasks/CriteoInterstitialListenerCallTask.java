package com.criteo.publisher.mediation.tasks;

import android.os.AsyncTask;
import android.webkit.URLUtil;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;

public class CriteoInterstitialListenerCallTask extends AsyncTask<Object, Void, Object> {

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    public CriteoInterstitialListenerCallTask(CriteoInterstitialAdListener listener) {
        this.criteoInterstitialAdListener = listener;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }
        Object object = objects[0];
        return object;
    }

    @Override
    protected void onPostExecute(Object object) {
        super.onPostExecute(object);
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
