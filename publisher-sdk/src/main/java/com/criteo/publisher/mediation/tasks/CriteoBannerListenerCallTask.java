package com.criteo.publisher.mediation.tasks;

import android.os.AsyncTask;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.Slot;


public class CriteoBannerListenerCallTask extends AsyncTask<Slot, Void, Slot> {

    private CriteoBannerView criteoBannerView;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerListenerCallTask(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        this.criteoBannerAdListener = listener;
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
        if (slot == null || !slot.isValid()) {
            criteoBannerAdListener.onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
        } else {
            criteoBannerAdListener.onAdFetchSucceeded(criteoBannerView);
        }
    }
}
