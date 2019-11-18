package com.criteo.publisher;

import com.criteo.publisher.model.NativeAssets;

public class CriteoNativeAd {
    private static final String TAG = CriteoNativeAd.class.getSimpleName();

    private NativeAssets nativeAssets;

    CriteoNativeAd(NativeAssets nativeAssets) {
        this.nativeAssets = nativeAssets;
    }

    public String getTitle() {
        return productExists(this.nativeAssets) ? this.nativeAssets.nativeProducts.get(0).title : null;
    }

    public String getPrice() {
        return productExists(this.nativeAssets) ? this.nativeAssets.nativeProducts.get(0).price : null;
    }

    public String getDescription() {
        return productExists(this.nativeAssets) ? this.nativeAssets.nativeProducts.get(0).description : null;
    }

    public String getCallToAction() {
        return productExists(this.nativeAssets) ? this.nativeAssets.nativeProducts.get(0).callToAction : null;
    }

    public String getAdvertiserDescription() {
        return this.nativeAssets != null ? this.nativeAssets.advertiserDescription : null;
    }

    public String getAdvertiserDomain() {
        return this.nativeAssets != null ? this.nativeAssets.advertiserDomain : null;
    }

    private boolean productExists(NativeAssets nativeAssets) {
        return nativeAssets != null &&
                nativeAssets.nativeProducts != null &&
                nativeAssets.nativeProducts.size() > 0;
    }
}
