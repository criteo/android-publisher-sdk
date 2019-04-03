package com.criteo.publisher.mediation.tasks;

import android.content.Context;
import android.os.AsyncTask;
import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.view.CriteoInterstitialView;
import java.lang.ref.WeakReference;

public class CriteoInterstitialFetchTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<CriteoInterstitialView> criteoInterstitialView;
    private Context context;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoInterstitialFetchTask(Context context, WeakReference<CriteoInterstitialView> interstitialView,
            CriteoBannerAdListener listener) {
        criteoInterstitialView = interstitialView;
        this.context = context;
        this.criteoBannerAdListener = listener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}