package com.criteo.publisher.mediation.tasks;

import android.os.AsyncTask;
import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.Slot;


public class CriteoBannerFetchTask extends AsyncTask<Slot, Void, Slot> {

    private CriteoBannerView criteoBannerView;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerFetchTask(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        this.criteoBannerAdListener = listener;
    }

    @Override
    protected Slot doInBackground(Slot... slots) {
        if (slots == null) {
            return null;
        }
        Slot slot = slots[0];
        return slot;
    }

    @Override
    protected void onPostExecute(Slot slot) {
        super.onPostExecute(slot);
        if (slot == null) {
            criteoBannerAdListener.onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
        } else {
            criteoBannerAdListener.onAdFetchSucceededForBanner(criteoBannerView);
        }
    }
}
