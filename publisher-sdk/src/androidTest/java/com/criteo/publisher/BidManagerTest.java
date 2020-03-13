package com.criteo.publisher;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.criteo.publisher.Util.AdvertisingInfo;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.bid.BidLifecycleListener;
import com.criteo.publisher.cache.SdkCache;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnitMapper;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.NativeAssets;
import com.criteo.publisher.model.NativeProduct;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.network.BidRequestSender;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class BidManagerTest {

  private static final String CRT_CPM = "crt_cpm";
  private static final String CRT_NATIVE_TITLE = "crtn_title";
  private static final String CRT_NATIVE_DESC = "crtn_desc";
  private static final String CRT_NATIVE_PRICE = "crtn_price";
  private static final String CRT_NATIVE_CLICK_URL = "crtn_clickurl";
  private static final String CRT_NATIVE_CTA = "crtn_cta";
  private static final String CRT_NATIVE_IMAGE_URL = "crtn_imageurl";
  private static final String CRT_NATIVE_ADV_NAME = "crtn_advname";
  private static final String CRT_NATIVE_ADV_DOMAIN = "crtn_advdomain";
  private static final String CRT_NATIVE_ADV_LOGO_URL = "crtn_advlogourl";
  private static final String CRT_NATIVE_ADV_URL = "crtn_advurl";
  private static final String CRT_NATIVE_PR_URL = "crtn_prurl";
  private static final String CRT_NATIVE_PR_IMAGE_URL = "crtn_primageurl";
  private static final String CRT_NATIVE_PR_TEXT = "crtn_prtext";
  private static final String CRT_NATIVE_PIXEL_URL = "crtn_pixurl_";
  private static final String CRT_NATIVE_PIXEL_COUNT = "crtn_pixcount";

  private static final String DFP_CRT_DISPLAY_URL = "crt_displayurl";

  private DeviceUtil deviceUtil;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    deviceUtil = new DeviceUtil(mock(Context.class), mock(AdvertisingInfo.class));
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndNoBidAvailable_DoNotEnrich() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    BidManager manager = spy(createBidManager());
    doReturn(null).when(manager).getBidForAdUnitAndPrefetch(adUnit);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    manager.enrichBid(builder, adUnit);
    PublisherAdRequest request = builder.build();

    assertNull(request.getCustomTargeting().getString(DFP_CRT_DISPLAY_URL));
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndNotNativeBidAvailable_EnrichBuilder() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(false);
    when(slot.getCpm()).thenReturn("0.10");
    when(slot.getDisplayUrl()).thenReturn("http://display.url");

    BidManager manager = spy(createBidManager());
    doReturn(slot).when(manager).getBidForAdUnitAndPrefetch(adUnit);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    manager.enrichBid(builder, adUnit);
    PublisherAdRequest request = builder.build();
    Bundle customTargeting = request.getCustomTargeting();

    assertEquals(2, customTargeting.size());
    assertEquals("0.10", customTargeting.get(CRT_CPM));
    assertEquals(encodeForDfp("http://display.url"), customTargeting.get(DFP_CRT_DISPLAY_URL));
  }

  @Test
  public void enrichBid_GivenDfpBuilderAndNativeBidAvailable_EnrichBuilder() throws Exception {
    BannerAdUnit adUnit = new BannerAdUnit("adUnit", new AdSize(1, 2));

    NativeProduct product1 = mock(NativeProduct.class);
    when(product1.getTitle()).thenReturn("title");
    when(product1.getDescription()).thenReturn("description");
    when(product1.getPrice()).thenReturn("$1337");
    when(product1.getClickUrl()).thenReturn("http://click.url");
    when(product1.getImageUrl()).thenReturn("http://image.url");
    when(product1.getCallToAction()).thenReturn("call to action");

    NativeProduct product2 = mock(NativeProduct.class);
    when(product2.getTitle()).thenReturn("unexpected title");

    NativeAssets nativeAssets = mock(NativeAssets.class);
    when(nativeAssets.getNativeProducts()).thenReturn(asList(product1, product2));
    when(nativeAssets.getAdvertiserDescription()).thenReturn("advertiser name");
    when(nativeAssets.getAdvertiserDomain()).thenReturn("advertiser domain");
    when(nativeAssets.getAdvertiserLogoClickUrl()).thenReturn("http://advertiser.url");
    when(nativeAssets.getAdvertiserLogoUrl()).thenReturn("http://advertiser.logo.url");
    when(nativeAssets.getPrivacyOptOutImageUrl()).thenReturn("http://privacy.image.url");
    when(nativeAssets.getPrivacyOptOutClickUrl()).thenReturn("http://privacy.url");
    when(nativeAssets.getPrivacyLongLegalText()).thenReturn("privacy legal text");
    when(nativeAssets.getImpressionPixels()).thenReturn(asList(
        "http://pixel.url/0",
        "http://pixel.url/1"));

    Slot slot = mock(Slot.class);
    when(slot.isNative()).thenReturn(true);
    when(slot.getCpm()).thenReturn("0.42");
    when(slot.getNativeAssets()).thenReturn(nativeAssets);

    BidManager manager = spy(createBidManager());
    doReturn(slot).when(manager).getBidForAdUnitAndPrefetch(adUnit);

    PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
    manager.enrichBid(builder, adUnit);
    PublisherAdRequest request = builder.build();
    Bundle customTargeting = request.getCustomTargeting();

    assertEquals(17, customTargeting.size());
    assertEquals("0.42", customTargeting.get(CRT_CPM));

    assertEquals(
        encodeForDfp("title"),
        customTargeting.get(CRT_NATIVE_TITLE));

    assertEquals(
        encodeForDfp("description"),
        customTargeting.get(CRT_NATIVE_DESC));

    assertEquals(
        encodeForDfp("$1337"),
        customTargeting.get(CRT_NATIVE_PRICE));

    assertEquals(
        encodeForDfp("http://click.url"),
        customTargeting.get(CRT_NATIVE_CLICK_URL));

    assertEquals(
        encodeForDfp("call to action"),
        customTargeting.get(CRT_NATIVE_CTA));

    assertEquals(
        encodeForDfp("http://image.url"),
        customTargeting.get(CRT_NATIVE_IMAGE_URL));

    assertEquals(
        encodeForDfp("advertiser name"),
        customTargeting.get(CRT_NATIVE_ADV_NAME));

    assertEquals(
        encodeForDfp("advertiser domain"),
        customTargeting.get(CRT_NATIVE_ADV_DOMAIN));

    assertEquals(
        encodeForDfp("http://advertiser.logo.url"),
        customTargeting.get(CRT_NATIVE_ADV_LOGO_URL));

    assertEquals(
        encodeForDfp("http://advertiser.url"),
        customTargeting.get(CRT_NATIVE_ADV_URL));

    assertEquals(
        encodeForDfp("http://privacy.url"),
        customTargeting.get(CRT_NATIVE_PR_URL));

    assertEquals(
        encodeForDfp("http://privacy.image.url"),
        customTargeting.get(CRT_NATIVE_PR_IMAGE_URL));

    assertEquals(
        encodeForDfp("privacy legal text"),
        customTargeting.get(CRT_NATIVE_PR_TEXT));

    assertEquals(
        encodeForDfp("http://pixel.url/0"),
        customTargeting.get(CRT_NATIVE_PIXEL_URL + "0"));

    assertEquals(
        encodeForDfp("http://pixel.url/1"),
        customTargeting.get(CRT_NATIVE_PIXEL_URL + "1"));

    assertEquals("2", customTargeting.get(CRT_NATIVE_PIXEL_COUNT));
  }

  private String encodeForDfp(String displayUrl) {
    return deviceUtil.createDfpCompatibleString(displayUrl);
  }

  @NonNull
  private BidManager createBidManager() {
    return new BidManager(
        mock(SdkCache.class),
        mock(Config.class),
        deviceUtil,
        mock(Clock.class),
        mock(AdUnitMapper.class),
        mock(BidRequestSender.class),
        mock(BidLifecycleListener.class)
    );
  }

}
