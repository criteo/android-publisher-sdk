package com.criteo.publisher.network;

import android.content.Context;
import android.util.Pair;
import com.criteo.publisher.Util.NetworkResponseListener;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.CacheAdUnit;
import com.criteo.publisher.model.Cdb;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CdbDownloadTaskTest {

    private CdbDownloadTask cdbDownloadTask;
    private List<CacheAdUnit> cacheAdUnits;

    @Mock
    private Context mContext;

    @Mock
    private NetworkResponseListener responseListener;

    @Mock
    private Hashtable<Pair<String, String>, CdbDownloadTask> bidsInCdbTask;

    @Test
    public void checkCacheRemove() {
        MockitoAnnotations.initMocks(this);
        cacheAdUnits = new ArrayList<>();
        CacheAdUnit cacheAdUnit = new CacheAdUnit(new AdSize(320, 50), "UniqueId");
        cacheAdUnits.add(cacheAdUnit);

        cdbDownloadTask = new CdbDownloadTask(mContext, responseListener, false,
                "Agent", cacheAdUnits, bidsInCdbTask);
        cdbDownloadTask.onPostExecute(null);

        String formattedSize = cacheAdUnit
                .getFormattedSize();
        Mockito.verify(bidsInCdbTask, Mockito.times(1)).remove(new Pair<>(cacheAdUnit.getPlacementId(),
                formattedSize));
    }


}