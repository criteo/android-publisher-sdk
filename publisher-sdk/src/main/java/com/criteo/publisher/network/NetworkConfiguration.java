package com.criteo.publisher.network;

import android.content.Context;
import com.criteo.publisher.R;

public class NetworkConfiguration {

  private final Context context;

  public NetworkConfiguration(Context context) {
    this.context = context;
  }

  String getCdbUrl() {
    return context.getString(R.string.cdb_url);
  }

  String getRemoteConfigUrl() {
    return context.getString(R.string.config_url);
  }

  String getEventUrl() {
    return context.getString(R.string.event_url);
  }

}
