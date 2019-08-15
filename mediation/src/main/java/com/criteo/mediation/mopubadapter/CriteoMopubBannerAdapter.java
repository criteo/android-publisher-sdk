package com.criteo.mediation.mopubadapter;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_FAILED;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import com.criteo.mediation.listener.MopubBannerListenerImpl;
import com.criteo.publisher.Criteo;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoInitException;
import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CriteoMopubBannerAdapter extends CustomEventBanner {

    private static final String TAG = CriteoMopubBannerAdapter.class.getSimpleName();
    protected static final String ADUNIT_ID = "adUnitId";
    protected static final String CRITEO_PUBLISHER_ID = "cpId";
    protected static final String MOPUB_WIDTH = "com_mopub_ad_width";
    protected static final String MOPUB_HEIGHT = "com_mopub_ad_height";
    private CriteoBannerView bannerView;

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener,
            Map<String, Object> localExtras, Map<String, String> serverExtras) {

        if (TextUtils.isEmpty(localExtras.toString()) || TextUtils.isEmpty(serverExtras.toString())) {
            MoPubLog.log(LOAD_FAILED, TAG, "Invalid Parameters");
            customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        AdSize adSize = getAdSize(localExtras);
        String criteoPublisherId = serverExtras.get(CRITEO_PUBLISHER_ID);

        if (adSize == null || criteoPublisherId == null) {
            MoPubLog.log(LOAD_FAILED, TAG, "Invalid Parameters");
            customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        String adUnitId = serverExtras.get(ADUNIT_ID);

        if (adUnitId == null) {
            MoPubLog.log(LOAD_FAILED, TAG, "Missing Adunit Id");
            customEventBannerListener.onBannerFailed(MoPubErrorCode.MISSING_AD_UNIT_ID);
            return;
        }

        try {
            Criteo.getInstance();
            BannerAdUnit bannerAdUnit = new BannerAdUnit(adUnitId, adSize);
            bannerView = new CriteoBannerView(context, bannerAdUnit);
            MopubBannerListenerImpl listener = new MopubBannerListenerImpl(customEventBannerListener);
            bannerView.setCriteoBannerAdListener(listener);
            bannerView.loadAd();
            MoPubLog.log(LOAD_ATTEMPTED, TAG, "Bannerview loading");
        } catch (Exception e) {
            List<AdUnit> adUnits = new ArrayList<>();

            try {
                Criteo.init((Application) (context.getApplicationContext()), criteoPublisherId, adUnits);
            } catch (CriteoInitException e1) {

            }
            MoPubLog.log(LOAD_FAILED, TAG, "Initialization failed");
            customEventBannerListener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
        }
    }

    private AdSize getAdSize(Map<String, Object> localExtras) {
        Object objHeight = localExtras.get(MOPUB_HEIGHT);
        Object objWidth = localExtras.get(MOPUB_WIDTH);
        if (objHeight == null || objWidth == null) {
            return null;
        }

        Integer height = (Integer) objHeight;
        Integer width = (Integer) objWidth;
        return new AdSize(width, height);
    }

    @Override
    protected void onInvalidate() {
        if (bannerView != null) {
            bannerView.destroy();
        }
    }
}
