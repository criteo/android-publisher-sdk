package com.criteo.publisher.tasks;

import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialListenerCallTaskTest {

    @Mock
    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    CriteoInterstitialListenerCallTask criteoInterstitialFetchTask;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        criteoInterstitialFetchTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
    }


    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        Slot slot = null;

        criteoInterstitialFetchTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdReceived();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testWithNullTokenValue() throws InterruptedException {
        TokenValue tokenValue = null;
        criteoInterstitialFetchTask = new CriteoInterstitialListenerCallTask(criteoInterstitialAdListener);
        criteoInterstitialFetchTask.execute(tokenValue);

        Thread.sleep(100);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdReceived();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

}