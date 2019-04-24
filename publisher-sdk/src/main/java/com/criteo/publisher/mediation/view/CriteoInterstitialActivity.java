package com.criteo.publisher.mediation.view;

import android.app.Activity;
import android.os.Bundle;
import com.criteo.publisher.R;

public class CriteoInterstitialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criteo_interstitial);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO dont forget to destroy webview
    }
}
