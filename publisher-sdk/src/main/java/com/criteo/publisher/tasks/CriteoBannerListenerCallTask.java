package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoListenerCode;

import java.lang.ref.WeakReference;

public class CriteoBannerListenerCallTask extends AsyncTask<Object, Void, Object> {
    private CriteoBannerAdListener adListener;
    private WeakReference<CriteoBannerView> view;

    /**
     * Task that calls the relevant callback in the criteoBannerAdListener
     * based on the CriteoListenerCode passed to execute.
     * Passes the criteoBannerView as a parameter to the onAdReceived callback if
     * the CriteoListenerCode is valid
     */
    public CriteoBannerListenerCallTask(CriteoBannerAdListener criteoBannerAdListener
            , CriteoBannerView criteoBannerView) {
        adListener = criteoBannerAdListener;
        this.view = new WeakReference<>(criteoBannerView);
    }

    @Override
    protected Object doInBackground(Object... objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }
        return objects[0];
    }

    @Override
    protected void onPostExecute(Object object) {
        if(adListener != null) {
            if (object == null) {
                adListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
            } else {
                CriteoListenerCode code = (CriteoListenerCode) object;
                switch (code) {
                    case INVALID:
                        adListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
                        break;
                    case VALID:
                        adListener.onAdReceived(view.get());
                        break;
                    case CLICK:
                        adListener.onAdLeftApplication();
                        break;
                }
            }
        }
    }
}
