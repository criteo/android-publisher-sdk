package com.criteo.publisher;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CriteoNativeAdView extends FrameLayout {


    private View titleView;
    private View descriptionView;
    private View priceView;
    private View calltoActionView;
    private CriteoMediaView logoView;
    private View advertiserDescriptionView;
    private View advertiserDomainView;
    private View adChoices;
    private CriteoNativeAd nativeAd;
    private CriteoMediaView productImageView;


    public CriteoNativeAdView(@NonNull Context context) {
        super(context);
        setupAdChoices();
    }

    private void setupAdChoices() {

    }

    public void setTitleView(View tView) {
        this.titleView = titleView;
    }

    public void setDescriptionView(View dView) {
        this.descriptionView = dView;
    }

    public void setPriceView(View priceView) {
        this.priceView = priceView;
    }

    public void setCalltoActionView(View calltoActionView) {
        this.calltoActionView = calltoActionView;
    }

    public void setLogoView(CriteoMediaView logoView) {
        this.logoView = logoView;
    }

    public void setAdvertiserDescriptionView(View advertiserDescriptionView) {
        this.advertiserDescriptionView = advertiserDescriptionView;
    }

    public void setAdvertiserDomainView(View advertiserDomainView) {
        this.advertiserDomainView = advertiserDomainView;
    }

    public CriteoMediaView getLogoView() {
        return logoView;
    }

    public View getAdChoices() {
        return adChoices;
    }

    public View getAdvertiserDescriptionView() {
        return advertiserDescriptionView;
    }

    public View getAdvertiserDomainView() {
        return advertiserDomainView;
    }

    public View getCalltoActionView() {
        return calltoActionView;
    }

    public View getDescriptionView() {
        return descriptionView;
    }

    public View getPriceView() {
        return priceView;
    }

    public View getTitleView() {
        return titleView;
    }

    public void setMediaView(CriteoMediaView mediaView) {

    }

    public CriteoMediaView getMediaView() {
        return productImageView;
    }

    public void setNativeAd(CriteoNativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }
}
