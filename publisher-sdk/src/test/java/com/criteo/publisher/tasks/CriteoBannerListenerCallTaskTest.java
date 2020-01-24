package com.criteo.publisher.tasks;

import static com.criteo.publisher.CriteoListenerCode.INVALID;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoListenerCode;
import java.lang.ref.WeakReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoBannerListenerCallTaskTest {

    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Mock
    CriteoBannerView criteoBannerView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void run_GivenNullListener_DoesNotCrash() throws Exception {
        criteoBannerAdListener = null;

        CriteoBannerListenerCallTask task = createTask(INVALID);

        assertThatCode(task::run).doesNotThrowAnyException();
    }

    @Test
    public void testWithInvalidCode() {
        CriteoBannerListenerCallTask task = createTask(INVALID);
        task.run();

        Mockito.verify(criteoBannerAdListener, Mockito.times(1))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdClicked();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLeftApplication();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdOpened();

    }

    @Test
    public void testWithValidCode() {
        CriteoBannerListenerCallTask task = createTask(CriteoListenerCode.VALID);
        task.run();

        Mockito.verify(criteoBannerAdListener, Mockito.times(0))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdClicked();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdLeftApplication();
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdOpened();
    }

    @Test
    public void testWithClickCode() {
        CriteoBannerListenerCallTask task = createTask(CriteoListenerCode.CLICK);
        task.run();

        Mockito.verify(criteoBannerAdListener, Mockito.times(0))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        Mockito.verify(criteoBannerAdListener, Mockito.times(0)).onAdReceived(criteoBannerView);
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdClicked();
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdLeftApplication();
        Mockito.verify(criteoBannerAdListener, Mockito.times(1)).onAdOpened();
    }

    private CriteoBannerListenerCallTask createTask(CriteoListenerCode code) {
        return new CriteoBannerListenerCallTask(
            criteoBannerAdListener,
            new WeakReference<>(criteoBannerView),
            code);
    }


}