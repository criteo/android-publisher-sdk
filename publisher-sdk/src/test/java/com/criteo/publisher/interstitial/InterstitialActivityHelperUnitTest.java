package com.criteo.publisher.interstitial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ResolveInfo;
import com.criteo.publisher.CriteoInterstitialAdListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InterstitialActivityHelperUnitTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Context context;

  @Mock
  private CriteoInterstitialAdListener listener;

  private InterstitialActivityHelper helper;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    helper = spy(new InterstitialActivityHelper(context));
  }

  @Test
  public void openActivity_GivenNotAvailableActivity_DoesNothing() throws Exception {
    doReturn(false).when(helper).isAvailable();

    helper.openActivity("myContent", listener);

    verifyZeroInteractions(context);
  }

  @Test
  public void openActivity_GivenAvailableActivity_StartActivity() throws Exception {
    doReturn(true).when(helper).isAvailable();

    helper.openActivity("myContent", listener);

    verify(context).startActivity(any());
  }

  @Test
  public void isAvailable_GivenUnresolvedActivity_ReturnFalse() throws Exception {
    when(context.getPackageManager().resolveActivity(any(), anyInt()))
        .thenReturn(null);

    boolean available = helper.isAvailable();

    assertThat(available).isFalse();
  }

  @Test
  public void isAvailable_GivenNotFoundLayout_ReturnFalse() throws Exception {
    when(context.getPackageName()).thenReturn("com.my.package");
    when(context.getResources().getIdentifier(
        "activity_criteo_interstitial",
        "layout",
        context.getPackageName()))
        .thenReturn(0);

    boolean available = helper.isAvailable();

    assertThat(available).isFalse();
  }

  @Test
  public void isAvailable_GivenResolvedActivityAndFoundLayout_ReturnTrue() throws Exception {
    when(context.getPackageManager().resolveActivity(any(), anyInt()))
        .thenReturn(mock(ResolveInfo.class));

    when(context.getPackageName()).thenReturn("com.my.package");
    when(context.getResources().getIdentifier(
        "activity_criteo_interstitial",
        "layout",
        context.getPackageName()))
        .thenReturn(42);

    boolean available = helper.isAvailable();

    assertThat(available).isTrue();
  }

}