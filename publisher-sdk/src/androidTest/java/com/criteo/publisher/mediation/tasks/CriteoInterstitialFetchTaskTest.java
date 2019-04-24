package com.criteo.publisher.mediation.tasks;

import com.criteo.publisher.mediation.listeners.CriteoInterstitialAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.model.Slot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialFetchTaskTest {

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    CriteoInterstitialFetchTask criteoInterstitialFetchTask;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        criteoInterstitialFetchTask = new CriteoInterstitialFetchTask(criteoInterstitialAdListener);
    }


    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        Slot slot = null;

        criteoInterstitialFetchTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdFetchSucceededForInterstitial();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testNotifyListenerAsyncWitSlot() throws InterruptedException {
        Slot slot = new Slot();
        criteoInterstitialFetchTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1)).onAdFetchSucceededForInterstitial();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }
}