/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.criteo.publisher;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;
import com.criteo.publisher.model.NativeAdUnit;

/**
 * List of ad units that could be used to get an valid answer from the CDB stub.
 * <p>
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

  /**
   * Note that the size could be anything
   */
  public static final BannerAdUnit BANNER_DEMO = new BannerAdUnit("30s6zt3ayypfyemwjvmp",
      new AdSize(400, 500));

  public static final InterstitialAdUnit INTERSTITIAL = new InterstitialAdUnit(
      "test-PubSdk-Interstitial");

  public static final InterstitialAdUnit INTERSTITIAL_UNKNOWN = new InterstitialAdUnit(
      "test-PubSdk-Unknown");

  public static final InterstitialAdUnit INTERSTITIAL_DEMO = new InterstitialAdUnit(
      "6yws53jyfjgoq1ghnuqb");

  public static final InterstitialAdUnit INTERSTITIAL_IBV_DEMO = new InterstitialAdUnit(
      "mf2v6pikq5vqdjdtfo3j");

  public static final NativeAdUnit NATIVE = new NativeAdUnit(
      "test-PubSdk-Native");

  public static final NativeAdUnit NATIVE_UNKNOWN = new NativeAdUnit(
      "test-PubSdk-Unknown");

}
