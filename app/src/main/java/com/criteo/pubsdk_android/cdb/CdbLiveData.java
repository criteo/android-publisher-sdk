package com.criteo.pubsdk_android.cdb;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;

import com.criteo.publisher.BuildConfig;
import com.criteo.publisher.Util.DeviceUtil;
import com.criteo.publisher.model.Cdb;
import com.criteo.publisher.model.Publisher;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.User;
import com.criteo.publisher.network.PubSdkNetwork;

import java.util.ArrayList;

public class CdbLiveData extends MutableLiveData<Cdb> {
    private CdbDownloadTask cdbDownloadTask;
    private Context mContext;

    public CdbLiveData(Context context) {
        cdbDownloadTask = new CdbDownloadTask();
        this.mContext = context;
    }

    public void loadCbdData(final String profile, final User user,
                            final Publisher publisher, final ArrayList<Slot> slots) {
        if (cdbDownloadTask.getStatus() != AsyncTask.Status.RUNNING) {
            cdbDownloadTask.execute(profile, user, publisher, slots);
        }
    }

    @Override
    protected void onInactive() {
        if (cdbDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
            cdbDownloadTask.cancel(true);
        }
        super.onInactive();
    }

    private class CdbDownloadTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            int profileId = (Integer) objects[0];
            User user = (User) objects[1];
            Publisher publisher = (Publisher) objects[2];
            ArrayList<Slot> slots = (ArrayList<Slot>) objects[3];
            Cdb cdb = new Cdb();
            cdb.setSlots(slots);
            cdb.setUser(user);
            cdb.setPublisher(publisher);
            cdb.setSdkVersion(String.valueOf(BuildConfig.VERSION_NAME));
            cdb.setProfileId(profileId);
            Cdb response = PubSdkNetwork.loadCdb(mContext, cdb, DeviceUtil.getUserAgent(mContext));
            postValue(response);
            return null;
        }
    }
}
