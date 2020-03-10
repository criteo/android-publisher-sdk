package com.criteo.publisher.Util;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.Slot;
import java.util.List;

public interface NetworkResponseListener {

  void setCacheAdUnits(@NonNull List<Slot> slots);

  void setTimeToNextCall(int seconds);
}
