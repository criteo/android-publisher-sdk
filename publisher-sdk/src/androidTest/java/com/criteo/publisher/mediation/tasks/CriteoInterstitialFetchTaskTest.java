package com.criteo.publisher.mediation.tasks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.criteo.publisher.listener.CriteoInterstitialAdListener;
import com.criteo.publisher.Util.CriteoErrorCode;
import com.criteo.publisher.model.Slot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoInterstitialFetchTaskTest {

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

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(0)).onAdFetchSucceeded();
        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testNotifyListenerAsyncWitSlotAndInvalidUrl() throws InterruptedException {
        Slot slot =mock(Slot.class);
        when(slot.getDisplayUrl()).thenReturn("!?+##!?");

        criteoInterstitialFetchTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoInterstitialAdListener, Mockito.times(1))
                .onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }
}