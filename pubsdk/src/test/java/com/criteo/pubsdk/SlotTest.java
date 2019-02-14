package com.criteo.pubsdk;

import com.criteo.pubsdk.model.Cdb;
import com.criteo.pubsdk.model.Slot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SlotTest {
    private JsonArray array;
    private List<Slot> slots;

    public SlotTest() {
        String json = "{\n" +
                "    \"slots\": [\n" +
                "        {\n" +
                "            \"placementId\": \"adunitid1\",\n" +
                "            \"cpm\": 1.1200000047683716,\n" +
                "            \"currency\": \"EUR\",\n" +
                "            \"width\": 300,\n" +
                "            \"height\": 250,\n" +
                "            \"ttl\": 0,\n" +
                "            \"creative\": \"<img src='https://demo.criteo.com/publishertag/preprodtest/creative.png' width='300' height='250' />\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"placementId\": \"adunitid2\",\n" +
                "            \"cpm\": 1.1200000047683716,\n" +
                "            \"currency\": \"EUR\",\n" +
                "            \"width\": 300,\n" +
                "            \"height\": 250,\n" +
                "            \"ttl\": 0,\n" +
                "            \"creative\": \"<img src='https://demo.criteo.com/publishertag/preprodtest/creative.png' width='300' height='250' />\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(json);
        array = element.getAsJsonObject().getAsJsonArray("slots");
        slots = new ArrayList<Slot>();
        for (int i = 0; i < array.size(); i++) {
            slots.add(new Slot(array.get(i).getAsJsonObject()));
        }
    }

    @Test
    public void jsonParsingSizeTest() {
        assertEquals(array.size(), slots.size());
    }

    @Test
    public void jsonParsingCurrencyTest() {
        assertEquals(array.get(0).getAsJsonObject().get("currency").getAsString(),
                slots.get(0).getCurrency());
    }

    @Test
    public void jsonParsingCreativeTest() {
        assertEquals(array.get(0).getAsJsonObject().get("creative").getAsString(),
                slots.get(0).getCreative());
    }

    @Test
    public void jsonParsingCpmTest() {
        String cdbResponse = "{\"slots\":[{\"placementId\":\"/140800857/Endeavour_320x50\",\"cpm\":1.12,\"currency\":\"EUR\",\"width\":320,\"height\":50,\"ttl\":300,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"},{\"placementId\":\"/140800857/Endeavour_Interstitial_320x480\",\"cpm\":\"1.12\",\"currency\":\"EUR\",\"width\":320,\"height\":480,\"ttl\":300,\"displayUrl\":\"https://publisherdirect.criteo.com/publishertag/preprodtest/FakeAJS.js\"}]}";
        JsonParser parser = new JsonParser();
        JsonObject responseObject = parser.parse(cdbResponse).getAsJsonObject();
        Cdb cdb = new Cdb(responseObject);
        assertEquals(cdb.getSlots().get(0).getCpm(), cdb.getSlots().get(1).getCpm());
    }
}
