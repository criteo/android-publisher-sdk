package com.criteo.mediation.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


@RunWith(AndroidJUnit4.class)
public class CriteoAdapterTest {

    private static final String CRITEO_PUBLISHER_ID_KEY = "cpid";
    private static final String ADUNITID_KEY = "adUnitId";
    private static final String PUBLISHER_ID = "B-056946";
    private static final String ADUNITID = "/140800857/Endeavour_Interstitial_320x480";

    private Context context;

    @Mock
    private CustomEventInterstitialListener listener;

    @Mock
    private MediationAdRequest mediationAdRequest;

    private Bundle customEventExtras;


    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getContext();
        MockitoAnnotations.initMocks(this);
        customEventExtras = new Bundle();
    }

    @Test
    public void requestInterstitialAdWithEmptyServerParams() {
        CriteoAdapter criteoAdapter = new CriteoAdapter();
        String serverParameter = "";
        criteoAdapter.requestInterstitialAd(context, listener, serverParameter, mediationAdRequest, customEventExtras);
        Mockito.verify(listener, Mockito.times(1)).onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);

    }

    @Test
    public void requestInterstitialAdWithNullCriteo() {
        CriteoAdapter criteoAdapter = new CriteoAdapter();
        String serverParameter = "{   \"" + CRITEO_PUBLISHER_ID_KEY + "\":" + PUBLISHER_ID + ",   \" " + ADUNITID_KEY
                + "\":\" " + ADUNITID + "  \" }";
        criteoAdapter.requestInterstitialAd(context, listener, serverParameter, mediationAdRequest, customEventExtras);
        Mockito.verify(listener, Mockito.times(1)).onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);

    }
}
