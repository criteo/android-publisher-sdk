package com.criteo.publisher;

import android.view.View;

public interface CriteoBannerAdListener extends CriteoAdListener {

  /**
   * Called when an ad is successfully fetched.
   */
  void onAdReceived(View view);
}
