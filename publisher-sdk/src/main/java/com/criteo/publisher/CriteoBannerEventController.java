package com.criteo.publisher;

import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.listener.CriteoBannerAdListener;
import com.criteo.publisher.tasks.CriteoBannerLoadTask;
import com.criteo.publisher.view.CriteoBannerView;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;


public class CriteoBannerEventController {

    private CriteoBannerView criteoBannerView;
    private CriteoBannerLoadTask criteoBannerLoadTask;
    private CriteoBannerAdListener criteoBannerAdListener;

    public CriteoBannerEventController(CriteoBannerView bannerView, CriteoBannerAdListener listener) {
        this.criteoBannerView = bannerView;
        this.criteoBannerAdListener = listener;
        bannerView.setCriteoBannerAdListener(listener);
    }


    public void fetchAdAsync(AdUnit adUnit) {
        Slot slot = null;
        if (adUnit != null) {
            slot = Criteo.getInstance().getBidForAdUnit(adUnit);
        }

        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerLoadTask.execute(slot);
    }

    public void fetchAdAsync(BidToken bidToken) {
        TokenValue tokenValue = Criteo.getInstance().getTokenValue(bidToken, AdUnitType.CRITEO_BANNER);

        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, criteoBannerAdListener);
        criteoBannerLoadTask.execute(tokenValue);
    }


}
