package com.criteo.publisher.model;

import org.json.JSONObject;

public class Config {

    public static final String mediationAdTag = "<html><body style='text-align:center; margin:0px; "
            + "padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>";
    private static final String KILL_SWITCH = "killSwitch";
    private static final String MEDIATION_TAG = "mediationTag";
    private boolean killSwitch;

    public Config() {

    }

    public Config(JSONObject json) {
        this.killSwitch = json.optBoolean(KILL_SWITCH, false);
   //     this.mediationAdTag = json.optString(MEDIATION_TAG, "<html><body style='text-align:center; margin:0px; "
   //             + "padding:0px; horizontal-align:center;'><script src=\"%%displayUrl%%\"></script></body></html>");
    }

    public boolean isKillSwitch() {
        return killSwitch;
    }

    public void setKillSwitch(boolean killSwitch) {
        this.killSwitch = killSwitch;
    }
}
