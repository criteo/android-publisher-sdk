package com.criteo.mediation.mopub;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.mopub.mobileads.CustomEventBanner;
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
public class CriteoMopubBannerAdapterTest {

    private static final String BANNER_ADUNIT_ID = "ca-app-pub-2995206374493561/3062725613";
    private static final String ADUNIT_ID = "adUnitId";
    private static final String CRITEO_PUBLISHER_ID = "cpId";
    private static final String MOPUB_WIDTH = "com_mopub_ad_width";
    private static final String MOPUB_HEIGHT = "com_mopub_ad_height";

    private Context context;
    private Map<String, Object> localExtras;
    private Map<String, String> serverExtras;
    private CriteoBannerAdapter criteoMopubBannerAdapter;

    @Mock
    private CustomEventBanner.CustomEventBannerListener customEventBannerListener;


    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getContext();
        MockitoAnnotations.initMocks(this);
        localExtras = new HashMap<String, Object>();
        serverExtras = new HashMap<String, String>();
    }

    // serverExtras and localExtras are empty
    @Test
    public void requestBannerAdWithEmptyParameters() {
        criteoMopubBannerAdapter = new CriteoBannerAdapter();
        criteoMopubBannerAdapter.loadBanner(context, customEventBannerListener, localExtras, serverExtras);

        Mockito.verify(customEventBannerListener, Mockito.times(1))
                .onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventBannerListener, Mockito.times(0))
                .onBannerFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
    }

    @Test
    public void requestBannerAdWithNullAdSize() {
        criteoMopubBannerAdapter = new CriteoBannerAdapter();
        serverExtras.put(ADUNIT_ID, BANNER_ADUNIT_ID);
        serverExtras.put(CRITEO_PUBLISHER_ID, "123");
        localExtras.put("Test", "local extras shouldnt be empty");

        criteoMopubBannerAdapter.loadBanner(context, customEventBannerListener, localExtras, serverExtras);
        Mockito.verify(customEventBannerListener, Mockito.times(1))
                .onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventBannerListener, Mockito.times(0))
                .onBannerFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
    }

    @Test
    public void requestBannerAdWithNullPublisherId() {
        criteoMopubBannerAdapter = new CriteoBannerAdapter();
        serverExtras.put(ADUNIT_ID, BANNER_ADUNIT_ID);
        localExtras.put(MOPUB_WIDTH, 320);
        localExtras.put(MOPUB_HEIGHT, 50);

        criteoMopubBannerAdapter.loadBanner(context, customEventBannerListener, localExtras, serverExtras);
        Mockito.verify(customEventBannerListener, Mockito.times(1))
                .onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventBannerListener, Mockito.times(0))
                .onBannerFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
    }

    @Test
    public void requestBannerAdWithNullAdUnitId() {
        criteoMopubBannerAdapter = new CriteoBannerAdapter();
        serverExtras.put(CRITEO_PUBLISHER_ID, "123");
        localExtras.put(MOPUB_WIDTH, 320);
        localExtras.put(MOPUB_HEIGHT, 50);

        criteoMopubBannerAdapter.loadBanner(context, customEventBannerListener, localExtras, serverExtras);
        Mockito.verify(customEventBannerListener, Mockito.times(0))
                .onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventBannerListener, Mockito.times(1))
                .onBannerFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
    }

    @Test
    public void requestBannerAdWithNullCriteo() {
        criteoMopubBannerAdapter = new CriteoBannerAdapter();
        serverExtras.put(CRITEO_PUBLISHER_ID, "123");
        serverExtras.put(ADUNIT_ID, BANNER_ADUNIT_ID);
        localExtras.put(MOPUB_WIDTH, 320);
        localExtras.put(MOPUB_HEIGHT, 50);

        criteoMopubBannerAdapter.loadBanner(context, customEventBannerListener, localExtras, serverExtras);
        Mockito.verify(customEventBannerListener, Mockito.times(0))
                .onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        Mockito.verify(customEventBannerListener, Mockito.times(0))
                .onBannerFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
        Mockito.verify(customEventBannerListener, Mockito.times(1))
                .onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
    }

}