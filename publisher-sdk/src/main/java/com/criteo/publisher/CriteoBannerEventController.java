package com.criteo.publisher;

import static com.criteo.publisher.CriteoListenerCode.CLICK;
import static com.criteo.publisher.CriteoListenerCode.INVALID;
import static com.criteo.publisher.CriteoListenerCode.VALID;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import com.criteo.publisher.tasks.CriteoBannerListenerCallTask;
import com.criteo.publisher.tasks.CriteoBannerLoadTask;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Executor;


public class CriteoBannerEventController {

    @NonNull
    private final WeakReference<CriteoBannerView> view;

    @Nullable
    private final CriteoBannerAdListener adListener;

    @NonNull
    private final Criteo criteo;

    public CriteoBannerEventController(
        @NonNull CriteoBannerView bannerView,
        @NonNull Criteo criteo) {
        this.view = new WeakReference<>(bannerView);
        this.adListener = bannerView.getCriteoBannerAdListener();
        this.criteo = criteo;
    }

    public void fetchAdAsync(@Nullable AdUnit adUnit) {
        Slot slot = criteo.getBidForAdUnit(adUnit);

        if (slot == null) {
            notifyFor(INVALID);
        } else {
            notifyFor(VALID);
            displayAd(slot.getDisplayUrl());
        }
    }

    public void fetchAdAsync(@Nullable BidToken bidToken) {
        TokenValue tokenValue = criteo.getTokenValue(bidToken, AdUnitType.CRITEO_BANNER);

        if (tokenValue == null) {
            notifyFor(INVALID);
        } else {
            notifyFor(VALID);
            displayAd(tokenValue.getDisplayUrl());
        }
    }

    private void notifyFor(@NonNull CriteoListenerCode code) {
        Executor threadPoolExecutor = DependencyProvider.getInstance().provideThreadPoolExecutor();
        CriteoBannerListenerCallTask listenerCallTask = new CriteoBannerListenerCallTask(
            adListener, view);
        listenerCallTask.executeOnExecutor(threadPoolExecutor, code);
    }

    @VisibleForTesting
    void displayAd(@NonNull String displayUrl) {
        CriteoBannerLoadTask loadTask = new CriteoBannerLoadTask(
            view, createWebViewClient(), criteo.getConfig());
        // Must run on UI thread as it is displaying the fetched ad
        Executor serialExecutor = DependencyProvider.getInstance().provideSerialExecutor();
        loadTask.executeOnExecutor(serialExecutor, displayUrl);
    }

    // WebViewClient is created here to prevent passing the AdListener everywhere.
    // Setting this webViewClient to the WebView is done in the CriteoBannerLoadTask as all
    // WebView methods need to run in the same UI thread
    @VisibleForTesting
    WebViewClient createWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Context context = view.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // this callback gets called after the user has clicked on the creative. In case of deeplink,
                // if the target application is not installed on the device, an ActivityNotFoundException
                // will be thrown. Therefore, an explicit check is made to ensure that there exists at least
                // one package that can handle the intent
                PackageManager packageManager = context.getPackageManager();
                List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                if (list.size() > 0) {
                    context.startActivity(intent);

                    notifyFor(CLICK);
                }

                return true;
            }
        };
    }

}
