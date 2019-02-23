package com.criteo.publisher.Util;

import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;

import java.util.List;

public interface NetworkResponseListener {
    void setThrottle(int throttle);

    void setAdUnits(List<Slot> Slots);

    void setConfig(Config config);

    void setTimeToNextCall(int seconds);
}
