package com.criteo.publisher.Util;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.Slot;
import java.util.List;
import org.json.JSONObject;

public interface NetworkResponseListener {

  void setCacheAdUnits(@NonNull List<Slot> slots);

  void refreshConfig(@NonNull JSONObject config);

  void setTimeToNextCall(int seconds);
}
