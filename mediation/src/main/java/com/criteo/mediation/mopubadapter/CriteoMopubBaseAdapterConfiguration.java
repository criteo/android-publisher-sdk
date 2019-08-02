package com.criteo.mediation.mopubadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mopub.common.BaseAdapterConfiguration;
import com.mopub.common.OnNetworkInitializationFinishedListener;
import com.mopub.mobileads.MoPubErrorCode;
import java.util.Map;

public class CriteoMopubBaseAdapterConfiguration extends BaseAdapterConfiguration {

    private String adapterVersion;
    private String networkSdkVersion;
    private String moPubNetworkName;


    @NonNull
    @Override
    public String getAdapterVersion() {
        return adapterVersion;
    }

    @Nullable
    @Override
    public String getBiddingToken(@NonNull Context context) {
        return null;
    }

    @NonNull
    @Override
    public String getMoPubNetworkName() {
        return moPubNetworkName;
    }

    @NonNull
    @Override
    public String getNetworkSdkVersion() {
        return networkSdkVersion;
    }

    @Override
    public void initializeNetwork(@NonNull Context context, @Nullable Map<String, String> configuration,
            @NonNull OnNetworkInitializationFinishedListener listener) {

        this.networkSdkVersion = "3.0.0";
        this.adapterVersion = "3.0.0.1";
        this.moPubNetworkName = "Criteo";

        listener.onNetworkInitializationFinished(CriteoMopubBaseAdapterConfiguration.class,
                MoPubErrorCode.ADAPTER_INITIALIZATION_SUCCESS);
    }
}
