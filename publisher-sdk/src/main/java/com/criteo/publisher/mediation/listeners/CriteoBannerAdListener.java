package com.criteo.publisher.mediation.listeners;

public interface CriteoBannerAdListener extends CriteoAdListener {

    /**
     * Called when an ad is successfully fetched.
     */
    public void onAdFetchSucceededForBanner();
}
