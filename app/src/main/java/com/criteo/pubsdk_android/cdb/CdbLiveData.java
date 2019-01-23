package com.criteo.pubsdk_android.cdb;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import com.criteo.pubsdk.model.Cdb;
import com.criteo.pubsdk.model.Publisher;
import com.criteo.pubsdk.model.Slot;
import com.criteo.pubsdk.model.User;
import com.criteo.pubsdk.network.PubSdkNetwork;

import java.util.ArrayList;

public class CdbLiveData extends MutableLiveData<Cdb> {
    private CdbDownloadTask task;

    public CdbLiveData() {
        task = new CdbDownloadTask();
    }

    public void loadCbdData(final String profile, final User user,
                            final Publisher publisher, final ArrayList<Slot> slots) {
        if (task.getStatus() != AsyncTask.Status.RUNNING) {
            task.execute(profile, user, publisher, slots);
        }
    }

    @Override
    protected void onInactive() {
        if (task.getStatus() == AsyncTask.Status.RUNNING) {
            task.cancel(true);
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
            cdb.setSdkVersion("2.3.0");
            cdb.setProfileId(profileId);
            Cdb response = PubSdkNetwork.loadCdb(cdb);
            postValue(response);
            return null;
        }
    }
}
