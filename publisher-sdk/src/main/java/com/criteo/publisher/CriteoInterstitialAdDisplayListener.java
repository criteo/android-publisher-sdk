package com.criteo.publisher;

public interface CriteoInterstitialAdDisplayListener {

  void onAdReadyToDisplay();

  void onAdFailedToDisplay(CriteoErrorCode error);

}
