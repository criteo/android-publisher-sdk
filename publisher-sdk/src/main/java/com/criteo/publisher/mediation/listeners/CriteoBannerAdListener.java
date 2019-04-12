package com.criteo.publisher.mediation.listeners;

import android.view.View;

public interface CriteoBannerAdListener extends CriteoAdListener {

    /**
     * Called when an ad is successfully fetched.
     */
    public void onAdFetchSucceededForBanner(View view);
}
