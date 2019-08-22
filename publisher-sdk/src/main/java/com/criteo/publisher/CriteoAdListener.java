package com.criteo.publisher;

public interface CriteoAdListener {

    /**
     * Called when an ad fetch fails.
     *
     * @param code The reason the fetch failed.
     */
    void onAdFailedToReceive(CriteoErrorCode code);

    /**
     * Called when an ad is clicked.
     */
    void onAdLeftApplication();

    void onAdClicked();

}
