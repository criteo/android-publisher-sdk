package com.criteo.publisher.Util;

import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;
import java.util.List;

public interface NetworkResponseListener {

    void setCacheAdUnits(List<Slot> slots);

    void setConfig(Config config);

    void setTimeToNextCall(int seconds);
}
