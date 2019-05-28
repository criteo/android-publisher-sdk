package com.criteo.publisher.mediation.tasks;

import android.os.AsyncTask;
import android.webkit.URLUtil;
import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.model.Slot;

public class CriteoInterstitialListenerCallTask extends AsyncTask<Slot, Void, Slot> {

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    public CriteoInterstitialListenerCallTask(CriteoInterstitialAdListener listener) {
        this.criteoInterstitialAdListener = listener;
    }

    @Override
    protected Slot doInBackground(Slot... slots) {
        if (slots == null || slots.length == 0) {
            return null;
        }
        Slot slot = slots[0];
        return slot;
    }

    @Override
    protected void onPostExecute(Slot slot) {
        super.onPostExecute(slot);
        if (slot == null || !URLUtil
                .isValidUrl(slot.getDisplayUrl())) {
            criteoInterstitialAdListener.onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
        }
    }
}
