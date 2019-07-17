package com.criteo.publisher.listener;

import com.criteo.publisher.Util.CriteoErrorCode;

public interface CriteoAdListener {

    /**
     * Called when an ad fetch fails.
     *
     * @param code The reason the fetch failed.
     */
    void onAdFailedToLoad(CriteoErrorCode code);

    /**
     * Called when an ad is clicked.
     */
    void onAdLeftApplication();

}
