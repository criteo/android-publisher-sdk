package com.criteo.publisher.Util;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;

public interface NetworkResponseListener {

  void onCdbResponse(@NonNull CdbRequest request, @NonNull CdbResponse response);

}
