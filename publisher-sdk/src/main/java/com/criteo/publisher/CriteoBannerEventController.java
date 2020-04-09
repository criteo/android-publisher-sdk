package com.criteo.publisher;

import android.support.annotation.Nullable;
import com.criteo.publisher.model.AdUnit;

public interface CriteoBannerEventController {

  void fetchAdAsync(@Nullable AdUnit adUnit);

  void fetchAdAsync(@Nullable BidToken bidToken);

}
