package com.criteo.pubsdk.model;

import com.google.gson.JsonObject;

public class Config {

  public static final String KILL_SWITCH = "killSwitch";
  private boolean killSwitch;

  public Config() {

  }

  public Config(JsonObject json) {
    this.killSwitch = json.has(KILL_SWITCH) ? json.get(KILL_SWITCH).getAsBoolean() : false;

  }

  public boolean isKillSwitch() {
    return killSwitch;
  }

  public void setKillSwitch(boolean killSwitch) {
    this.killSwitch = killSwitch;
  }
}
