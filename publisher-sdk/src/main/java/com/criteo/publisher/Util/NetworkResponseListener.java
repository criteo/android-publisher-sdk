package com.criteo.publisher.Util;

import android.support.annotation.NonNull;
import com.criteo.publisher.model.Slot;

import org.json.JSONObject;

import java.util.List;

public interface NetworkResponseListener {

    void setCacheAdUnits(@NonNull List<Slot> slots);

    void refreshConfig(JSONObject config);

    void setTimeToNextCall(int seconds);
}
