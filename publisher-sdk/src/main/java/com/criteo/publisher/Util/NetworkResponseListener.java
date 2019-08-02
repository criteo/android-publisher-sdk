package com.criteo.publisher.Util;

import com.criteo.publisher.model.Slot;

import org.json.JSONObject;

import java.util.List;

public interface NetworkResponseListener {

    void setCacheAdUnits(List<Slot> slots);

    void refreshConfig(JSONObject config);

    void setTimeToNextCall(int seconds);
}
