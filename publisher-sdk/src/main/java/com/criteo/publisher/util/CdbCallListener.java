package com.criteo.publisher.util;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.CdbRequest;
import com.criteo.publisher.model.CdbResponse;

public interface CdbCallListener {

  void onCdbRequest(@NonNull CdbRequest request);

  void onCdbResponse(@NonNull CdbRequest request, @NonNull CdbResponse response);

  void onCdbError(@NonNull CdbRequest request, @NonNull Exception exception);
}
