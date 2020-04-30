package com.criteo.publisher.headerbidding;

import static com.criteo.publisher.concurrent.ThreadingUtil.callOnMainThreadAndWait;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.support.test.rule.ActivityTestRule;
import com.criteo.publisher.BidManager;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.test.activity.DummyActivity;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MoPubHeaderBiddingTest {

  @Rule
  public ActivityTestRule<DummyActivity> activityRule = new ActivityTestRule<>(DummyActivity.class);

  @Mock
  private BidManager bidManager;

  private MoPubHeaderBidding headerBidding;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    headerBidding = new MoPubHeaderBidding(bidManager);
  }

  @Test
  public void canHandle_GivenSimpleObject_ReturnFalse() throws Exception {
    boolean handling = headerBidding.canHandle(mock(Object.class));

    assertFalse(handling);
  }

  @Test
  public void canHandle_GivenMoPubView_ReturnTrue() throws Exception {
    MoPubView moPub = givenMoPubView();

    boolean handling = headerBidding.canHandle(moPub);

    assertTrue(handling);
  }

  @Test
  public void canHandle_GivenMoPubInterstitial_ReturnTrue() throws Exception {
    MoPubInterstitial moPub = givenMoPubInterstitial();

    boolean handling = headerBidding.canHandle(moPub);

    assertTrue(handling);
  }

  @Test
  public void enrichBid_GivenNotHandledObject_DoNothing() throws Exception {
    Object builder = mock(Object.class);

    headerBidding.enrichBid(builder, mock(AdUnit.class));

    verifyNoMoreInteractions(bidManager);
  }

  @Test
  public void enrichBid_GivenNoBidAvailableAndMoPubView_DoNotEnrich() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(null);

    MoPubView moPub = spy(givenMoPubView());
    headerBidding.enrichBid(moPub, adUnit);

    verifyNoInteractions(moPub);
  }

  @Test
  public void enrichBid_GivenNoBidAvailableAndMoPubInterstitial_DoNotEnrich() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(null);

    MoPubInterstitial moPub = spy(givenMoPubInterstitial());
    headerBidding.enrichBid(moPub, adUnit);

    verifyNoInteractions(moPub);
  }

  @Test
  public void enrichBid_GivenMoPubViewAndBannerBidAvailable_EnrichBuilder() throws Exception {
    AdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(42, 1337));

    Slot slot = mock(Slot.class);
    when(slot.getCpm()).thenReturn("0.10");
    when(slot.getDisplayUrl()).thenReturn("http://display.url");
    when(slot.getWidth()).thenReturn(42);
    when(slot.getHeight()).thenReturn(1337);

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    MoPubView moPub = givenMoPubView();
    moPub.setKeywords("previousData");
    headerBidding.enrichBid(moPub, adUnit);
    String keywords = moPub.getKeywords();

    assertEquals("previousData,crt_cpm:0.10,crt_displayUrl:http://display.url,crt_size:42x1337", keywords);
  }

  @Test
  public void enrichBid_GivenMoPubInterstitialAndInterstitialBidAvailable_EnrichBuilder() throws Exception {
    AdUnit adUnit = new InterstitialAdUnit("adUnit");

    Slot slot = mock(Slot.class);
    when(slot.getCpm()).thenReturn("0.10");
    when(slot.getDisplayUrl()).thenReturn("http://display.url");

    when(bidManager.getBidForAdUnitAndPrefetch(adUnit)).thenReturn(slot);

    MoPubInterstitial moPub = givenMoPubInterstitial();
    moPub.setKeywords("previousData");
    headerBidding.enrichBid(moPub, adUnit);
    String keywords = moPub.getKeywords();

    assertEquals("previousData,crt_cpm:0.10,crt_displayUrl:http://display.url", keywords);
  }

  private MoPubView givenMoPubView() {
    return callOnMainThreadAndWait(() -> new MoPubView(activityRule.getActivity()));
  }

  private MoPubInterstitial givenMoPubInterstitial() {
    return callOnMainThreadAndWait(() ->
        new MoPubInterstitial(activityRule.getActivity(), "adUnit"));
  }

}