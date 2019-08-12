package com.criteo.mediation.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.criteo.publisher.CriteoBannerView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


@RunWith(AndroidJUnit4.class)
public class CriteoGoogleAdapterTest {

    private static final String CRITEO_PUBLISHER_ID_KEY = "cpid";
    private static final String ADUNITID_KEY = "adUnitId";
    private static final String PUBLISHER_ID = "B-056946";
    private static final String ADUNITID = "/140800857/Endeavour_Interstitial_320x480";

    private Context context;

    @Mock
    private CustomEventInterstitialListener interstitialListener;

    @Mock
    private CustomEventBannerListener bannerAdlistener;

    @Mock
    private MediationAdRequest mediationAdRequest;

    private Bundle customEventExtras;
    private CriteoGoogleAdapter criteoGoogleAdapter;


    @Before
    public void setUp() {
        context = InstrumentationRegistry.getContext();
        criteoGoogleAdapter = new CriteoGoogleAdapter();
        MockitoAnnotations.initMocks(this);
        customEventExtras = new Bundle();
    }

    @After
    public void tearDown() {
        context = null;
        criteoGoogleAdapter = null;
        customEventExtras = null;
    }

    @Test
    public void requestBannerAdWithEmptyServerParams() {
        String serverParameter = "";
        criteoGoogleAdapter
                .requestBannerAd(context, bannerAdlistener, serverParameter, new AdSize(320, 480), mediationAdRequest,
                        customEventExtras);
        Mockito.verify(bannerAdlistener, Mockito.times(1)).onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
    }

    @Test
    public void requestBannerAdWithNullCriteo() {
        String serverParameter = "{   \"" + CRITEO_PUBLISHER_ID_KEY + "\":" + PUBLISHER_ID + ",   \" " + ADUNITID_KEY
                + "\":\" " + ADUNITID + "  \" }";
        criteoGoogleAdapter
                .requestBannerAd(context, bannerAdlistener, serverParameter, new AdSize(320, 480), mediationAdRequest,
                        customEventExtras);
        Mockito.verify(bannerAdlistener, Mockito.times(1)).onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);

    }

    @Test
    public void requestInterstitialAdWithEmptyServerParams() {
        String serverParameter = "";
        criteoGoogleAdapter
                .requestInterstitialAd(context, interstitialListener, serverParameter, mediationAdRequest,
                        customEventExtras);
        Mockito.verify(interstitialListener, Mockito.times(1)).onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);

    }

    @Test
    public void requestInterstitialAdWithNullCriteo() {
        String serverParameter = "{   \"" + CRITEO_PUBLISHER_ID_KEY + "\":" + PUBLISHER_ID + ",   \" " + ADUNITID_KEY
                + "\":\" " + ADUNITID + "  \" }";
        criteoGoogleAdapter
                .requestInterstitialAd(context, interstitialListener, serverParameter, mediationAdRequest,
                        customEventExtras);
        Mockito.verify(interstitialListener, Mockito.times(1)).onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);

    }
}
