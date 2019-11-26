package com.criteo.publisher.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.DependencyProvider;
import com.criteo.publisher.Util.AdUnitType;
import com.criteo.publisher.Util.MockedDependenciesRule;
import com.criteo.publisher.model.Config;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CriteoBannerLoadTaskTest {
    @Rule
    public MockedDependenciesRule mockedDependenciesRule  = new MockedDependenciesRule();

    private static final String DISPLAY_URL = "displayUrl";
    private static final String CPM = "cpm";
    private static final String PLACEMENT_ID = "placementId";
    private static final String displayUrl = "<html><body style='text-align:center; margin:0px; padding:0px; horizontal-align:center;'><script src=\"https://www.criteo.com\"></script></body></html>";


    @Mock
    private CriteoBannerAdListener criteoBannerAdListener;

    @Mock
    private CriteoBannerView criteoBannerView;

    private CriteoBannerLoadTask criteoBannerLoadTask;

    private Config config;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(criteoBannerView.getSettings()).thenReturn(new TestWebSettings());
        config = new Config(InstrumentationRegistry.getContext());
        DependencyProvider dependencyProvider = mockedDependenciesRule.getDependencyProvider();
        when(dependencyProvider.provideConfig(any(Context.class))).thenReturn(config);
    }


    @Test
    public void testWithNullSlot() throws InterruptedException {
        Slot slot = null;
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, new WebViewClient(), config);
        criteoBannerLoadTask.execute(slot);

        Thread.sleep(100);

        verify(criteoBannerAdListener, times(0)).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(0))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        verify(criteoBannerView, times(0))
                .loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
    }

    @Test
    public void testNotifyListenerAsyncWithInvalidSlot() throws InterruptedException {
        JSONObject response = new JSONObject();
        try {
            response.put("cpm", "abc");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Slot slot = new Slot(response);

        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, new WebViewClient(), config);
        criteoBannerLoadTask.execute(slot);

        Thread.sleep(100);

        verify(criteoBannerAdListener, times(0)).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(0)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        verify(criteoBannerView, times(0))
                .loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
    }

    @Test
    public void testWithValidSlot() throws InterruptedException, JSONException {
        JSONObject response = new JSONObject();
        response.put(PLACEMENT_ID, "/140800857/Endeavour_320x50");
        response.put(CPM, "10.0");
        response.put(DISPLAY_URL, "https://www.criteo.com");
        Slot slot = new Slot(response);
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, new WebViewClient(), config);
        criteoBannerLoadTask.execute(slot);

        Thread.sleep(100);

        verify(criteoBannerAdListener, times(0)).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(0)).onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        verify(criteoBannerView, times(1))
                .loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
    }

    @Test
    public void testWithValidTokenValue() throws InterruptedException {
        TokenValue tokenValue = new TokenValue(System.currentTimeMillis(), 500, "https://www.criteo.com",
                AdUnitType.CRITEO_BANNER);
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, new WebViewClient(), config);
        criteoBannerLoadTask.execute(tokenValue);

        Thread.sleep(100);

        verify(criteoBannerAdListener, times(0)).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(0))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        verify(criteoBannerView, times(1))
                .loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");

    }


    @Test
    public void testWithNullTokenValue() throws InterruptedException {
        TokenValue tokenValue = null;
        criteoBannerLoadTask = new CriteoBannerLoadTask(criteoBannerView, new WebViewClient(), config);
        criteoBannerLoadTask.execute(tokenValue);

        Thread.sleep(100);

        verify(criteoBannerAdListener, times(0)).onAdReceived(criteoBannerView);
        verify(criteoBannerAdListener, times(0))
                .onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
        verify(criteoBannerView, times(0))
                .loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
    }


    private class TestWebSettings extends WebSettings {

        @Override
        public void setSupportZoom(boolean support) {

        }

        @Override
        public boolean supportZoom() {
            return false;
        }

        @Override
        public void setMediaPlaybackRequiresUserGesture(boolean require) {

        }

        @Override
        public boolean getMediaPlaybackRequiresUserGesture() {
            return false;
        }

        @Override
        public void setBuiltInZoomControls(boolean enabled) {

        }

        @Override
        public boolean getBuiltInZoomControls() {
            return false;
        }

        @Override
        public void setDisplayZoomControls(boolean enabled) {

        }

        @Override
        public boolean getDisplayZoomControls() {
            return false;
        }

        @Override
        public void setAllowFileAccess(boolean allow) {

        }

        @Override
        public boolean getAllowFileAccess() {
            return false;
        }

        @Override
        public void setAllowContentAccess(boolean allow) {

        }

        @Override
        public boolean getAllowContentAccess() {
            return false;
        }

        @Override
        public void setLoadWithOverviewMode(boolean overview) {

        }

        @Override
        public boolean getLoadWithOverviewMode() {
            return false;
        }

        @Override
        public void setEnableSmoothTransition(boolean enable) {

        }

        @Override
        public boolean enableSmoothTransition() {
            return false;
        }

        @Override
        public void setSaveFormData(boolean save) {

        }

        @Override
        public boolean getSaveFormData() {
            return false;
        }

        @Override
        public void setSavePassword(boolean save) {

        }

        @Override
        public boolean getSavePassword() {
            return false;
        }

        @Override
        public void setTextZoom(int textZoom) {

        }

        @Override
        public int getTextZoom() {
            return 0;
        }

        @Override
        public void setDefaultZoom(ZoomDensity zoom) {

        }

        @Override
        public ZoomDensity getDefaultZoom() {
            return null;
        }

        @Override
        public void setLightTouchEnabled(boolean enabled) {

        }

        @Override
        public boolean getLightTouchEnabled() {
            return false;
        }

        @Override
        public void setUseWideViewPort(boolean use) {

        }

        @Override
        public boolean getUseWideViewPort() {
            return false;
        }

        @Override
        public void setSupportMultipleWindows(boolean support) {

        }

        @Override
        public boolean supportMultipleWindows() {
            return false;
        }

        @Override
        public void setLayoutAlgorithm(LayoutAlgorithm l) {

        }

        @Override
        public LayoutAlgorithm getLayoutAlgorithm() {
            return null;
        }

        @Override
        public void setStandardFontFamily(String font) {

        }

        @Override
        public String getStandardFontFamily() {
            return null;
        }

        @Override
        public void setFixedFontFamily(String font) {

        }

        @Override
        public String getFixedFontFamily() {
            return null;
        }

        @Override
        public void setSansSerifFontFamily(String font) {

        }

        @Override
        public String getSansSerifFontFamily() {
            return null;
        }

        @Override
        public void setSerifFontFamily(String font) {

        }

        @Override
        public String getSerifFontFamily() {
            return null;
        }

        @Override
        public void setCursiveFontFamily(String font) {

        }

        @Override
        public String getCursiveFontFamily() {
            return null;
        }

        @Override
        public void setFantasyFontFamily(String font) {

        }

        @Override
        public String getFantasyFontFamily() {
            return null;
        }

        @Override
        public void setMinimumFontSize(int size) {

        }

        @Override
        public int getMinimumFontSize() {
            return 0;
        }

        @Override
        public void setMinimumLogicalFontSize(int size) {

        }

        @Override
        public int getMinimumLogicalFontSize() {
            return 0;
        }

        @Override
        public void setDefaultFontSize(int size) {

        }

        @Override
        public int getDefaultFontSize() {
            return 0;
        }

        @Override
        public void setDefaultFixedFontSize(int size) {

        }

        @Override
        public int getDefaultFixedFontSize() {
            return 0;
        }

        @Override
        public void setLoadsImagesAutomatically(boolean flag) {

        }

        @Override
        public boolean getLoadsImagesAutomatically() {
            return false;
        }

        @Override
        public void setBlockNetworkImage(boolean flag) {

        }

        @Override
        public boolean getBlockNetworkImage() {
            return false;
        }

        @Override
        public void setBlockNetworkLoads(boolean flag) {

        }

        @Override
        public boolean getBlockNetworkLoads() {
            return false;
        }

        @Override
        public void setJavaScriptEnabled(boolean flag) {

        }

        @Override
        public void setAllowUniversalAccessFromFileURLs(boolean flag) {

        }

        @Override
        public void setAllowFileAccessFromFileURLs(boolean flag) {

        }

        @Override
        public void setPluginState(PluginState state) {

        }

        @Override
        public void setDatabasePath(String databasePath) {

        }

        @Override
        public void setGeolocationDatabasePath(String databasePath) {

        }

        @Override
        public void setAppCacheEnabled(boolean flag) {

        }

        @Override
        public void setAppCachePath(String appCachePath) {

        }

        @Override
        public void setAppCacheMaxSize(long appCacheMaxSize) {

        }

        @Override
        public void setDatabaseEnabled(boolean flag) {

        }

        @Override
        public void setDomStorageEnabled(boolean flag) {

        }

        @Override
        public boolean getDomStorageEnabled() {
            return false;
        }

        @Override
        public String getDatabasePath() {
            return null;
        }

        @Override
        public boolean getDatabaseEnabled() {
            return false;
        }

        @Override
        public void setGeolocationEnabled(boolean flag) {

        }

        @Override
        public boolean getJavaScriptEnabled() {
            return false;
        }

        @Override
        public boolean getAllowUniversalAccessFromFileURLs() {
            return false;
        }

        @Override
        public boolean getAllowFileAccessFromFileURLs() {
            return false;
        }

        @Override
        public PluginState getPluginState() {
            return null;
        }

        @Override
        public void setJavaScriptCanOpenWindowsAutomatically(boolean flag) {

        }

        @Override
        public boolean getJavaScriptCanOpenWindowsAutomatically() {
            return false;
        }

        @Override
        public void setDefaultTextEncodingName(String encoding) {

        }

        @Override
        public String getDefaultTextEncodingName() {
            return null;
        }

        @Override
        public void setUserAgentString(@Nullable String ua) {

        }

        @Override
        public String getUserAgentString() {
            return null;
        }

        @Override
        public void setNeedInitialFocus(boolean flag) {

        }

        @Override
        public void setRenderPriority(RenderPriority priority) {

        }

        @Override
        public void setCacheMode(int mode) {

        }

        @Override
        public int getCacheMode() {
            return 0;
        }

        @Override
        public void setMixedContentMode(int mode) {

        }

        @Override
        public int getMixedContentMode() {
            return 0;
        }

        @Override
        public void setOffscreenPreRaster(boolean enabled) {

        }

        @Override
        public boolean getOffscreenPreRaster() {
            return false;
        }

        @Override
        public void setSafeBrowsingEnabled(boolean enabled) {

        }

        @Override
        public boolean getSafeBrowsingEnabled() {
            return false;
        }

        @Override
        public void setDisabledActionModeMenuItems(int menuItems) {

        }

        @Override
        public int getDisabledActionModeMenuItems() {
            return 0;
        }
    }
}