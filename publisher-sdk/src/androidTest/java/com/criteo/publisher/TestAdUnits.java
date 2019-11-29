package com.criteo.publisher;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;

/**
 * List of ad units that could be used to get an valid answer from the CDB stub.
 *
 * Except for the _UNKNOWN ad units, it's guaranteed that the CDB stub will respond a bid to those.
 * For the _UNKNOWN ones, then the CDB stubs will always answer a no-bid.
 */
public class TestAdUnits {

  public static final BannerAdUnit BANNER_320_50 = new BannerAdUnit("test-PubSdk-Base",
      new AdSize(320, 50));

  public static final BannerAdUnit BANNER_320_480 = new BannerAdUnit("test-PubSdk-Base",
      new AdSize(320, 480));

  public static final BannerAdUnit BANNER_UNKNOWN = new BannerAdUnit("test-PubSdk-Unknown",
      new AdSize(320, 50));

  public static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
      "test-PubSdk-Interstitial");

}
