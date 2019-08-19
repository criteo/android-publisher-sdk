package com.criteo.mediation.mopub;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.criteo.mediation.mopub.CriteoInterstitialAdapter;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class CriteoMopubInterstitialAdapterTest {

    private static final String BANNER_ADUNIT_ID = "86c36b6223ce4730acf52323de3baa93";
    private static final String ADUNIT_ID = "adUnitId";
    private static final String CRITEO_PUBLISHER_ID = "cpId";
    private static final String MOPUB_WIDTH = "com_mopub_ad_width";
    private static final String MOPUB_HEIGHT = "com_mopub_ad_height";

    private Context context;
    private Map<String, Object> localExtras;
    private Map<String, String> serverExtras;
    private CriteoInterstitialAdapter criteoMopubInterstitialAdapter;

    @Mock
    private CustomEventInterstitial.CustomEventInterstitialListener customEventInterstitialListener;

    @Before
    public void setUp(){
        context = InstrumentationRegistry.getContext();
        MockitoAnnotations.initMocks(this);
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
        criteoMopubInterstitialAdapter = new CriteoInterstitialAdapter();
    }

    // serverExtras and localExtras are empty
    @Test
    public void requestInterstitialAdWithEmptyParameters() {

        criteoMopubInterstitialAdapter.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);

        Mockito.verify(customEventInterstitialListener, Mockito.times(1))
                .onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventInterstitialListener, Mockito.times(0))
                .onInterstitialFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
    }

    @Test
    public void requestInterstitialAdWithNullPublisherId() {
        serverExtras.put(ADUNIT_ID, BANNER_ADUNIT_ID);

        criteoMopubInterstitialAdapter.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        Mockito.verify(customEventInterstitialListener, Mockito.times(1))
                .onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventInterstitialListener, Mockito.times(0))
                .onInterstitialFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
    }

    @Test
    public void requestBannerAdWithNullAdUnitId() {
        serverExtras.put(CRITEO_PUBLISHER_ID, "123");

        criteoMopubInterstitialAdapter.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        Mockito.verify(customEventInterstitialListener, Mockito.times(0))
                .onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventInterstitialListener, Mockito.times(1))
                .onInterstitialFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
    }

    @Test
    public void requestBannerAdWithNullCriteo() {
        serverExtras.put(CRITEO_PUBLISHER_ID, "123");
        serverExtras.put(ADUNIT_ID, BANNER_ADUNIT_ID);
        localExtras.put(MOPUB_WIDTH, 320);
        localExtras.put(MOPUB_HEIGHT, 50);

        criteoMopubInterstitialAdapter.loadInterstitial(context, customEventInterstitialListener, localExtras, serverExtras);
        Mockito.verify(customEventInterstitialListener, Mockito.times(0))
                .onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventInterstitialListener, Mockito.times(0))
                .onInterstitialFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
        Mockito.verify(customEventInterstitialListener, Mockito.times(1))
                .onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
    }

}