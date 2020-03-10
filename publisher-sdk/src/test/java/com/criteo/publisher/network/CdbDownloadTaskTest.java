package com.criteo.publisher.network;

import static com.criteo.publisher.Util.AdUnitType.CRITEO_BANNER;
import static org.mockito.Mockito.verify;

import android.support.annotation.NonNull;
import com.criteo.publisher.Util.LoggingUtil;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.CdbRequestFactory;
import com.criteo.publisher.model.RemoteConfigRequestFactory;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CdbDownloadTaskTest {

  private List<CacheAdUnit> cacheAdUnits;

  @Mock
  private NetworkResponseListener responseListener;

  @Mock
  private Hashtable<CacheAdUnit, CdbDownloadTask> bidsInCdbTask;

  @Mock
  private LoggingUtil loggingUtil;

  @Mock
  private PubSdkApi api;

  private boolean isConfigRequested;

  private boolean isCdbRequested;

  @Mock
  private CdbRequestFactory cdbRequestFactory;

  @Mock
  private RemoteConfigRequestFactory remoteConfigRequestFactory;

  private final AtomicInteger adUnitId = new AtomicInteger(0);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    isConfigRequested = false;
    isCdbRequested = true;

    cacheAdUnits = new ArrayList<>();
  }

  @Test
  public void onPostExecute_GivenAdUnitsToLoadAndFailure_RemoveThemFromPendingTasks() {
    CacheAdUnit adUnit1 = createAdUnit();
    CacheAdUnit adUnit2 = createAdUnit();

    cacheAdUnits.add(adUnit1);
    cacheAdUnits.add(adUnit2);

    CdbDownloadTask cdbDownloadTask = createTask();
    cdbDownloadTask.onPostExecute(null);

    verify(bidsInCdbTask).remove(adUnit1);
    verify(bidsInCdbTask).remove(adUnit2);
  }

  @NonNull
  private CacheAdUnit createAdUnit() {
    String adUnitId = "adUnit #" + this.adUnitId.incrementAndGet();
    return new CacheAdUnit(new AdSize(320, 50), adUnitId, CRITEO_BANNER);
  }

  @NonNull
  private CdbDownloadTask createTask() {
    return new CdbDownloadTask(
        responseListener,
        isConfigRequested,
        isCdbRequested,
        cacheAdUnits,
        bidsInCdbTask,
        loggingUtil,
        api,
        cdbRequestFactory,
        remoteConfigRequestFactory);
  }
}
