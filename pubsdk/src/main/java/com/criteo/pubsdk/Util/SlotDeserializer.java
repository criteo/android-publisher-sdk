package com.criteo.pubsdk.Util;

import com.criteo.pubsdk.model.Slot;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class SlotDeserializer implements JsonDeserializer<Slot> {

    @Override
    public Slot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return new Slot(json.getAsJsonObject());
    }
}

