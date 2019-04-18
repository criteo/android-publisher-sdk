package com.criteo.publisher.mediation.tasks;

import com.criteo.publisher.mediation.listeners.CriteoBannerAdListener;
import com.criteo.publisher.mediation.utils.CriteoErrorCode;
import com.criteo.publisher.mediation.view.CriteoBannerView;
import com.criteo.publisher.model.Slot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoBannerFetchTaskTest {

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Mock
    private CriteoBannerView criteoBannerView;

    CriteoBannerFetchTask criteoBannerFetchTask;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        criteoBannerFetchTask = new CriteoBannerFetchTask(criteoBannerView, criteoBannerAdListener);
    }


    @Test
    public void testNotifyListenerAsyncWithNullSlot() throws InterruptedException {
        Slot slot = null;

        criteoBannerFetchTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdFetchSucceededForBanner(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }

    @Test
    public void testNotifyListenerAsyncWitSlot() throws InterruptedException {
        Slot slot = new Slot();
        criteoBannerFetchTask.execute(slot);

        Thread.sleep(100);

        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdFetchSucceededForBanner(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdFetchFailed(CriteoErrorCode.ERROR_CODE_NO_FILL);
    }
}