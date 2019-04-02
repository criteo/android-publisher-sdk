package com.criteo.publisher.mediation.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.view.CriteoBannerView;


public class CriteoBannerFetchTask extends AsyncTask<Void, Void, Void> {

    private CriteoBannerView criteoBannerView;
    private Context context;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerFetchTask(Context context, CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
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
