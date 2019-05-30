package com.criteo.pubsdk_android;

import static com.mopub.common.logging.MoPubLog.LogLevel.DEBUG;
import static com.mopub.common.logging.MoPubLog.LogLevel.INFO;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.Util.BannerAdUnit;
import com.criteo.publisher.model.AdSize;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.mobileads.MoPubView;

public class MopubActivity extends AppCompatActivity {

    private MoPubView publisherAdView;
    private LinearLayout linearLayout;
    private static final String AD_UNIT = "b5acf501d2354859941b13030d2d848a";
    private static final String CRT_DISPLAY_URL = "crt_displayUrl";
    private static final String CRT_DISPLAY_CREATIVE = "https://rdi.us.criteo.com/delivery/r/ajs.php?did=5c9d31c92edc9dc67203ee38b6f43a00&u=%7CgfenuDf6GAt0T4ax9G2074VyZUuINvEBORTJkP12BIo%3D%7C&c1=fYGSyyN4O4mkT2ynhzfwbdpiG7v0SMGpyk_8mUbwNIml5THlXxSIQO_JQSnOsUNK2v58ibue4bk7EcIIU1dEF0RbfDQ4KzirymhRPnuZLWBkvVfFezrMwdYTaTH23G4-HEgtuf6PuQgOSCpErOm8LzlaTECFqL4xfFsuAg-1I0K5XZMzGMzWs6aJLWRK0XergQrbg95DEUOLZ37n5AAwFZP3TpzfL3D97JU8jfx2Vp6rs70dolZJHXTd-uUeQJwNBgb2TEnItXx7a5vqk7ssFiPjoVX3NOcJVeMXw5wLYXyJw06-odGbGqfAoMIAkXvTmM1cw-lFf8BYgWoRzd7CTiVDtjo69dZ5mIOme7yDxh2dDHrGNYi6KYgl6Zt8UMOlnSZ4Be4mngs1k8RzVTFZPX78UP3uPUa4i7d5xG3kKXd8n6Fcfwu1_EJBilXjg7uV-4AJT62UFTQ";
    private Criteo criteo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mopub);

        //MoPub.setLocationAwareness(MoPub.LocationAwareness.TRUNCATED);
        //MoPub.setLocationPrecision(4);
        criteo = Criteo.getInstance();

        final SdkConfiguration.Builder configBuilder = new SdkConfiguration.Builder("b195f8dd8ded45fe847ad89ed1d016da");
        if (BuildConfig.DEBUG) {
            configBuilder.withLogLevel(DEBUG);
        } else {
            configBuilder.withLogLevel(INFO);
        }
        MoPub.initializeSdk(this, configBuilder.build(), new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                Log.d("test", "Succss");
            }
        });

        linearLayout = ((LinearLayout) findViewById(R.id.adViewHolder));
        findViewById(R.id.buttonBanner).setOnClickListener((View v) -> {
            onBannerClick();
        });

    }

    private void onBannerClick() {
        BannerAdUnit moPub = new BannerAdUnit("b5acf501d2354859941b13030d2d848a", new AdSize(50, 320));
        linearLayout.setBackgroundColor(Color.RED);
        linearLayout.removeAllViews();
        linearLayout.setVisibility(View.VISIBLE);
        publisherAdView = new MoPubView(this);
        criteo.setBidsForAdUnit(publisherAdView, moPub);
        publisherAdView.setAdUnitId(AD_UNIT);

        //PublisherAdRequest request = Criteo.getInstance().setBidsForAdUnit(builder, adUnit).build();
        publisherAdView.loadAd();
        linearLayout.addView(publisherAdView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        publisherAdView.destroy();
    }
}
