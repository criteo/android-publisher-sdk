package com.criteo.publisher.Util;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.criteo.publisher.CriteoInterstitialAdListener;

public class CriteoResultReceiver extends ResultReceiver {

    public static final String INTERSTITIAL_ACTION = "Action";
    public static final int RESULT_CODE_SUCCESSFUL = 100;
    public static final int ACTION_CLICKED = 200;
    public static final int ACTION_CLOSED = 201;

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    /**
     * Create a new ResultReceive to receive results.  Your {@link #onReceiveResult} method will be called from the
     * thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     */
    public CriteoResultReceiver(Handler handler, CriteoInterstitialAdListener listener) {
        super(handler);
        this.criteoInterstitialAdListener = listener;
    }

    //...
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultCode == RESULT_CODE_SUCCESSFUL && criteoInterstitialAdListener != null) {
            int action = resultData.getInt(INTERSTITIAL_ACTION);

            switch (action) {
                case ACTION_CLICKED:
                    criteoInterstitialAdListener.onAdLeftApplication();
                    break;

                case ACTION_CLOSED:
                    criteoInterstitialAdListener.onAdClosed();
                    break;

                default:
                    break;
            }
        }
    }
}
