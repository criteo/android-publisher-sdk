# Next version

- Features
  - MoPub Header-Bidding: Handle subclasses of `MoPubView` and `MoPubInterstitial`

- Bug fixes
  - Fix wrong interstitial orientation when user starts the application in landscape

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
