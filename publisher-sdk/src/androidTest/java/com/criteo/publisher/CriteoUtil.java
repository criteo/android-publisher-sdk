package com.criteo.publisher;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import com.criteo.publisher.model.AdUnit;
import java.util.Arrays;
import java.util.List;

public class CriteoUtil {

  public static final String TEST_CP_ID = "B-000001";

  private static final String CACHED_KILL_SWITCH = "CriteoCachedKillSwitch";

  public static Criteo givenInitializedCriteo(AdUnit... preloadedAdUnits) throws CriteoInitException {
    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    clearCriteo();

    return Criteo.init(app, TEST_CP_ID, Arrays.asList(preloadedAdUnits));
  }

  /**
   * Clear any side effects from previous calls due to the Criteo singleton.
   */
  public static void clearCriteo() {
    Criteo.setInstance(null);
  }

  public static Criteo.Builder getCriteoBuilder(AdUnit... preloadedAdUnits) throws CriteoInitException {
    Application app = (Application) InstrumentationRegistry.getTargetContext()
        .getApplicationContext();

    // clears any side effects from previous calls
    Criteo.setInstance(null);

    List<AdUnit> adUnits = Arrays.asList(preloadedAdUnits);
    Criteo.Builder builder = new Criteo.Builder(app, TEST_CP_ID).adUnits(adUnits);
    return builder;
  }
  /**
   * Clear all states retained in shared preferences used by the SDK.
   */
  public static void clearSharedPreferences() {
    SharedPreferences sharedPreferences = getSharedPreferences();

    sharedPreferences.edit()
        .remove(CACHED_KILL_SWITCH)
        .apply();
  }

  private static SharedPreferences getSharedPreferences() {
    Context context = InstrumentationRegistry.getTargetContext().getApplicationContext();

    String sharedPreferencesName = context.getString(R.string.shared_preferences);
    return context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
  }

}
