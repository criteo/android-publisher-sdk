package com.criteo.publisher;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.tasks.CriteoBannerLoadTask;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import java.lang.ref.WeakReference;


public class CriteoBannerEventController {

    private WeakReference<CriteoBannerView> criteoBannerView;
    private CriteoBannerLoadTask criteoBannerLoadTask;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerEventController(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = new WeakReference<>(bannerView);
        this.criteoBannerAdListener = listener;
        bannerView.setCriteoBannerAdListener(listener);
    }


    public void fetchAdAsync(AdUnit adUnit) {
        Slot slot = null;
        if (adUnit != null) {
            slot = Criteo.getInstance().getBidForAdUnit(adUnit);
        }

        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView.get(), criteoBannerAdListener);
        criteoBannerLoadTask.execute(slot);
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = Criteo.getInstance().getTokenValue(bidToken, AdUnitType.CRITEO_BANNER);

        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView.get(), criteoBannerAdListener);
        criteoBannerLoadTask.execute(tokenValue);
    }


}
