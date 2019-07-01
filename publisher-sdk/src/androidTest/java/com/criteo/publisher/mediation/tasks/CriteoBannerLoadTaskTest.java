package com.criteo.publisher.mediation.tasks;

import android.webkit.WebViewClient;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoBannerLoadTaskTest {

    private static final String DISPLAY_URL = "displayUrl";
    private static final String CPM = "cpm";
    private static final String PLACEMENT_ID = "placementId";
    private static final String displayUrl ="<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"https://www.criteo.com\"></script></body></html>";


    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Mock
    private CriteoBannerView criteoBannerView;

    private CriteoBannerLoadTask criteoBannerLoadTask;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testWithNullSlot() throws InterruptedException {
        Slot slot = null;
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, criteoBannerAdListener,
                new WebViewClient());
        criteoBannerLoadTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1))
                .onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testNotifyListenerAsyncWithInvalidSlot() throws InterruptedException {
        JSONObject response = new JSONObject();
        try {
            response.put("cpm", "abc");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Slot slot = new Slot(response);

        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, criteoBannerAdListener,
                new WebViewClient());
        criteoBannerLoadTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testWithValidSlot() throws InterruptedException, JSONException {
        JSONObject response = new JSONObject();
        response.put(PLACEMENT_ID, "/140800857/Endeavour_320x50");
        response.put(CPM, "10.0");
        response.put(DISPLAY_URL, "https://www.criteo.com");
        Slot slot = new Slot(response);
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, criteoBannerAdListener,
                new WebViewClient());
        criteoBannerLoadTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerView, Mockito.times(1)).loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
    }

    @Test
    public void testWithValidTokenValue() throws InterruptedException {
        TokenValue tokenValue = new TokenValue(System.currentTimeMillis(), 500, "https://www.criteo.com",
                AdUnitType.CRITEO_BANNER);
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, criteoBannerAdListener,
                new WebViewClient());
        criteoBannerLoadTask.execute(tokenValue);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0))
                .onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerView, Mockito.times(1)).loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");

    }


    @Test
    public void testWithNullTokenValue() throws InterruptedException {
        TokenValue tokenValue = null;
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, criteoBannerAdListener,
                new WebViewClient());
        criteoBannerLoadTask.execute(tokenValue);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1))
                .onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}