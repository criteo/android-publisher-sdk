# Next Version
- Bump AdMob to v22.1.0

# Version 4.9.2
- Fix proguard issue with `RemoteConfigResponse`
- Add null checks in `TopActivityFinder`

# Version 4.9.1
- Add support of Android 13 (API level 33)

# Version 4.9.0
- Add support of Android 13 (API level 33)

# Version 4.8.1
- Fix error on Criteo init

# Version 4.8.0
- Add support of Android 12L (API level 32)
- Bump AdMob to v21.2.0
- Bump minSdkVersion to 19

# Version 4.7.0
- Deprecate API related to MoPub and stop supporting MoPub App Bidding
- Add ability to set Children’s Online Privacy Protection Act (“COPPA”) flag
- Fix ANR when fetching User Agent from WebView

# Version 4.6.1

- Bump AGP, Gradle and Kotlin version
- Bump Java Compiler version to 11
- Continuously test support of Android 11 (API level 30)
- Add support of Android 12 (API level 31)

# Version 4.6.0

- Features
  - MoPub AppBidding: Add support for VAST Rewarded video ads
  - GAM AppBidding: Add support for VAST Rewarded video ads

# Version 4.5.0
- Features
  - MoPub AppBidding: Add support for VAST Video ads
  - GAM AppBidding: Add support for VAST Video ads
- Bug fixes
  - If SDK init is called from a worker thread, then GAID is fetched asynchronously in another worker thread to not 
    block the caller's thread.
  - Recover from corrupted network sending queue having an element with a huge size that could throw an OOM.

# Version 4.4.0
- Artifacts are now delivered through `Maven Central` repository instead of `JCenter` which is deprecated.
- Handle retro compatibility of AdMob < v19.7.0

# Version 4.3.0
- Breaking changes
  - Removed support of Google SDK < v19.7.0 (see https://developers.google.com/admob/android/migration)
- Features
  - Added support of Google SDK v20.0.0
  - Added support of Android 11 (API level 30)
- Bug fixes
  - Reduce log level of network exception from error to info
  - Fix infinite `NoSuchMethodException` when Proguard is used with method inlining enabled
  - Estimate size of in-memory queue and bound its size to avoid potential OOM

# Version 4.2.2

- Bug fixes
  - Reduce log level of `UnknownHostException` from error to info

# Version 4.2.1

- Bug fixes
  - Fix crash when initializing the SDK

# Version 4.2.0
- Features
  - Add remote logging support.

# Version 4.1.0

- Features
  - Add a constructor without `NativeAdUnit` in `CriteoNativeLoader` for InHouse integration
  - Add API to collect different levels of signals which will be used to bid based on context
  - Add `Criteo.Builder#debugLogsEnabled` to enable useful logs to troubleshoot the SDK

# Version 4.0.0

- Breaking changes
  - Remove `Context` parameter from `CriteoInterstitial` constructor
  - Remove `Parcelable` implementation from `AdSize` class
  - Move `CriteoInterstitialAdDisplayListener#onAdFailedToDisplay(CriteoErrorCode)` to
   `CriteoInterstitialAdListener#onAdFailedToReceive(CriteoErrorCode)`. This callback is fired
    when an error happens after requesting an interstitial ad.
  - Move `CriteoInterstitialAdDisplayListener#onAdReadyToDisplay()` to
   `CriteoInterstitialAdListener#onAdReceived(CriteoInterstitial)`. This callback is fired when an
    interstitial ad is ready to be displayed.
  - Provide default implementation in interstitial listener. Java 8 is required, see
  https://developer.android.com/studio/write/java8-support.
  - Remove the deprecated `Criteo#init` method. [`Criteo.Builder#init`](https://github.com/criteo/android-publisher-sdk/blob/main/publisher-sdk/src/main/java/com/criteo/publisher/Criteo.java#L54)
  should be used instead.
  - Update InHouse API:
    - Replace `Criteo#getBidResponse(AdUnit)` method by `Criteo#loadBid(AdUnit, BidResponseListener)`
    - Rename `BidResponse` to `Bid`
    - Remove `BidResponse#isBidSuccess()` method, bid is a success when `Bid` object is not null
    - Remove `BidResponse#getBidToken()` method and `BidToken` class, the `loadAd` methods (in `CriteoBannerView`,
    `CriteoInterstitial`, `CriteoNativeLoader`) take a `Bid` object instead
  - Update AppBidding API:
    - Replace `Criteo#setBidsForAdUnit(Object, AdUnit)` method by `Criteo#enrichAdObjectWithBid(Object, Bid)`
    - Use `Criteo#loadBid(AdUnit, BidResponseListener)` method to provide `Bid` object
  - Make `CriteoNativeAdListener` an interface instead of an abstract class
  - Replace `View` parameter by a `CriteoBannerView` in `CriteoBannerAdListener#onAdReceived`
  - Remove `CriteoBannerAdListener#onAdClosed`
  - Remove `CriteoBannerAdListener#onAdOpened`

- Features
  - CriteoBannerView is now a custom view that can be included directly in a layout file.
  - Add live bidding: Load a fresh bid within a pre-determined time budget and use any valid cached bid as a fallback.
  - Add `Criteo#getVersion()` method returning the version of the SDK at runtime.

# Version 3.10.1
- Bug fix
 - Ensure `CriteoInterstitialActivity` does not crash when the `application` object is null.
  
# Version 3.10.0

- Features
  - MoPub Header-Bidding: Handle subclasses of `MoPubView` and `MoPubInterstitial`
  - Server Side bidding:  `loadAdWithDisplayData` added to `CriteoBannerView` & `CriteoInterstitial`
- Bug fixes
  - Fix wrong interstitial orientation when user starts the application in landscape

# Version 3.9.0

- Features
  - Fetch User-Agent asynchronously to speed up the SDK initialization.
  - MoPub Header-Bidding: clean MoPub's keyword to support Auto-Refreshing banners

# Version 3.8.0

- Features
  - Provide legal privacy text for native in `CriteoNativeAd#getLegalText`
  - Artifacts are now delivered through `JCenter` repository instead of a custom one: from this
  version, the line `maven { url "https://pubsdk-bin.criteo.com/publishersdk/android" }` can be
  removed.

# Version 3.7.0

- Features
  - *Advanced native ads* public release; integration instructions and documentation available on
  our [support website](https://publisherdocs.criteotilt.com/app/android/)

# Version 3.6.0

- Features
  - Insert `crt_size` keywords for DFP, MoPub and Custom Header-Bidding integration on banner
  - Insert `crt_size` keywords for DFP Header-Bidding integration on interstitial
  - Deactivate some debug logs in release build

- Bug fixes
  - Fix all known memory leaks
