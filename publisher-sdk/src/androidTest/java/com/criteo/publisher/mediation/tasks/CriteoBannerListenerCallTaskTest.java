package com.criteo.publisher.mediation.tasks;

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

public class CriteoBannerListenerCallTaskTest {

    private static final String DISPLAY_URL = "displayUrl";
    private static final String CPM = "cpm";
    private static final String PLACEMENT_ID = "placementId";

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Mock
    private CriteoBannerView criteoBannerView;

    private CriteoBannerListenerCallTask criteoBannerListenerCallTask;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testWithNullSlot() throws InterruptedException {
        Slot slot = null;
        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerListenerCallTask.execute(slot);

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

        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerListenerCallTask.execute(slot);

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
        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerListenerCallTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testWithValidTokenValue() throws InterruptedException {
        TokenValue tokenValue = new TokenValue(System.currentTimeMillis(), 500, "https://www.criteo.com",
                AdUnitType.CRITEO_BANNER);
        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerListenerCallTask.execute(tokenValue);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0))
                .onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }


    @Test
    public void testWithNullTokenValue() throws InterruptedException {
        TokenValue tokenValue = null;
        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerListenerCallTask.execute(tokenValue);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLoaded(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1))
                .onAdFailedToLoad(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}