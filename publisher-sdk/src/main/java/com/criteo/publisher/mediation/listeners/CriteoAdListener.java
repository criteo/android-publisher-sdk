package com.criteo.publisher.mediation.listeners;

import com.criteo.publisher.mediation.utils.CriteoErrorCode;

public interface CriteoAdListener {

    /**
     * Called when an ad fetch fails.
     *
     * @param code The reason the fetch failed.
     */
    public void onAdFetchFailed(CriteoErrorCode code);

    /**
     * Called when an ad goes full screen.
     */
    public void onAdFullScreen();

    /**
     * Called when an ad is closed.
     */
    public void onAdClosed();

}
