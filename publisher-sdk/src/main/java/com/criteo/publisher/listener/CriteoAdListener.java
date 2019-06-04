package com.criteo.publisher.listener;

import com.criteo.publisher.Util.CriteoErrorCode;

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

    /**
     * Called when an ad is clicked.
     */
    public void onAdClicked();

}
