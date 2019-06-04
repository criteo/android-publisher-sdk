package com.criteo.publisher.listener;

import android.view.View;

public interface CriteoBannerAdListener extends CriteoAdListener {

    /**
     * Called when an ad is successfully fetched.
     */
    public void onAdFetchSucceeded(View view);
}
