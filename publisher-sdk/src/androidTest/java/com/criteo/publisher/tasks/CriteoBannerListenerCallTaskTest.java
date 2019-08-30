package com.criteo.publisher.tasks;

import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoListenerCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoBannerListenerCallTaskTest {

    private CriteoBannerListenerCallTask criteoBannerListenerCallTask;

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Mock
    CriteoBannerView criteoBannerView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        criteoBannerListenerCallTask = new CriteoBannerListenerCallTask(criteoBannerAdListener,
                criteoBannerView);
    }

    @Test
    public void testWithNullCriteoListenerCode() {
        criteoBannerListenerCallTask.onPostExecute(null);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdClicked();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLeftApplication();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdOpened();
    }

    @Test
    public void testWithInvalidCode() {
        criteoBannerListenerCallTask.onPostExecute(CriteoListenerCode.INVALID);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdClicked();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLeftApplication();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdOpened();

    }

    @Test
    public void testWithValidCode() {
        criteoBannerListenerCallTask.onPostExecute(CriteoListenerCode.VALID);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdClicked();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLeftApplication();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdOpened();
    }

    @Test
    public void testWithClickCode() {
        criteoBannerListenerCallTask.onPostExecute(CriteoListenerCode.CLICK);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdClicked();
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdLeftApplication();
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdOpened();
    }


}