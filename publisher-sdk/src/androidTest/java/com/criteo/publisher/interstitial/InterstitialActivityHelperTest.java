package com.criteo.publisher.interstitial;

import static com.criteo.publisher.interstitial.InterstitialActivityHelper.RESULT_RECEIVER;
import static com.criteo.publisher.interstitial.InterstitialActivityHelper.WEB_VIEW_DATA;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ComponentName;
import android.content.Context;
import com.criteo.publisher.CriteoInterstitialActivity;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.Util.CriteoResultReceiver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InterstitialActivityHelperTest {

  @Mock
  private Context context;

  @Mock
  private CriteoInterstitialAdListener listener;

  private InterstitialActivityHelper helper;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    helper = spy(new InterstitialActivityHelper());
  }

  @Test
  public void openActivity_GivenListenerAndContent_StartActivityWithThem() throws Exception {
    when(context.getPackageName()).thenReturn("myPackage");
    ComponentName expectedComponent = new ComponentName(context, CriteoInterstitialActivity.class);

    CriteoResultReceiver expectedReceiver = mock(CriteoResultReceiver.class);
    doReturn(expectedReceiver).when(helper).createReceiver(listener);

    helper.openActivity(context, "myContent", listener);

    verify(context).startActivity(argThat(intent -> {
      assertEquals(expectedComponent, intent.getComponent());
      assertEquals("myContent", intent.getStringExtra(WEB_VIEW_DATA));
      assertEquals(expectedReceiver, intent.getParcelableExtra(RESULT_RECEIVER));
      return true;
    }));
  }

}