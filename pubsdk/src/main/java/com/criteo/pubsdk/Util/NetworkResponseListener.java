package com.criteo.pubsdk.Util;

import com.criteo.pubsdk.model.Config;
import com.criteo.pubsdk.model.Slot;

import java.util.List;

public interface NetworkResponseListener {
    void setThrottle(int throttle);

    void setAdUnits(List<Slot> Slots);

    void setConfig(Config config);
}
