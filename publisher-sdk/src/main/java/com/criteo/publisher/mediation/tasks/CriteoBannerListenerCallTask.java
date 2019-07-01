package com.criteo.publisher.mediation.tasks;

import android.os.AsyncTask;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;


public class CriteoBannerListenerCallTask extends AsyncTask<Object, Void, Object> {

    private CriteoBannerView criteoBannerView;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerListenerCallTask(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        this.criteoBannerAdListener = listener;
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
        if(criteoBannerAdListener != null) {
            if (object == null) {
                criteoBannerAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                return;
            }
            if (object instanceof Slot) {
                Slot slot = (Slot) object;
                if (!slot.isValid()) {
                    criteoBannerAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                } else {
                    criteoBannerAdListener.onAdLoaded(criteoBannerView);
                }
            } else if (object instanceof TokenValue) {
                TokenValue tokenValue = (TokenValue) object;
                if (tokenValue == null) {
                    criteoBannerAdListener.onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
                } else {
                    criteoBannerAdListener.onAdLoaded(criteoBannerView);
                }
            }
        }
    }
}
