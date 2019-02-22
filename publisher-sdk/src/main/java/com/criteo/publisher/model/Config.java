package com.criteo.publisher.model;

import org.json.JSONObject;

public class Config {

    private static final String KILL_SWITCH = "killSwitch";
    private boolean killSwitch;

    public Config() {

    }

    public Config(JSONObject json) {
        this.killSwitch = json.optBoolean(KILL_SWITCH, false);

    }

    public boolean isKillSwitch() {
        return killSwitch;
    }

    public void setKillSwitch(boolean killSwitch) {
        this.killSwitch = killSwitch;
    }
}
