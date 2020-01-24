package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.criteo.publisher.model.Config;
import java.lang.ref.Reference;

/**
 * /!\ Don't remove this class unless you know what you are doing. /!\
 *
 * <p>
 *   Although no long running task is being run, this AsyncTask is still necessary to prevent
 *   deadlocks when invoking callbacks registered by the downstream application. This can happen
 *   when non-reentrant locks are used.
 *
 *   <pre>
 *     <code>
 *       private Mutex _lock = new Lock();
 *
 *       public void callCriteoSDK() {
 *          _lock.lock();
 *          sdk.callSdk(this);  //SDK immediately calls back with status
 *          _lock.unlock();
 *       }
 *
 *       public void onSuccess(Status status) {
 *          _lock.lock();
 *          handleSuccessCallback(status);
 *          _lock.unlock();
 *      }
 *
 *      // Discussion:
 *      //
 *      // If the onSuccess callback is invoked from
 *      // the same callstack as callCriteoSDK(), _lock
 *      // will not be released.  The subsequent call to
 *      // lock() may block depending on the lock's
 *      // implementation.
 *     </code>
 *   </pre>
 *
 *   More information can be found on @see <a href=https://jira.criteois.com/browse/EE-574">EE-574</a>
 * </p>
 */
public class CriteoBannerLoadTask extends AsyncTask<Object, Void, Object> {

    private static final String TAG = "Criteo.BLT";

    @NonNull
    private final Reference<? extends WebView> webViewRef;

    @NonNull
    private final Config config;

    @NonNull
    private WebViewClient webViewClient;

    /**
     * Taking WebViewClient as a constructor as all WebView/CriteoBannerView methods must be called on the same UI
     * thread. WebView.getSettings().setJavaScriptEnabled() & WebView.setWebViewClient() throws if not done in the
     * onPostExecute() as onPostExecute runs on the UI thread
     */
    public CriteoBannerLoadTask(
        @NonNull Reference<? extends WebView> webViewRef,
        @NonNull WebViewClient webViewClient,
        @NonNull Config config) {
        this.webViewRef = webViewRef;
        this.webViewClient = webViewClient;
        this.config = config;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            if (objects == null || objects.length == 0) {
                return null;
            }

            return objects[0];
        } catch (Throwable tr) {
            Log.e(TAG, "Internal BLT exec error.", tr);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object object) {
        try {
            if (object instanceof String) {
                String displayUrl = (String) object;
                loadWebview(displayUrl);
            }
        } catch (Throwable tr) {
            Log.e(TAG, "Internal BLT exec error.", tr);
        }
    }

    private void loadWebview(@NonNull String url) {
        WebView criteoBannerView = webViewRef.get();
        if (criteoBannerView != null) {
            criteoBannerView.getSettings().setJavaScriptEnabled(true);
            criteoBannerView.setWebViewClient(this.webViewClient);
            String displayUrlWithTag = config.getAdTagUrlMode();
            String displayUrl = displayUrlWithTag.replace(config.getDisplayUrlMacro(), url);
            criteoBannerView.loadDataWithBaseURL("", displayUrl, "text/html", "UTF-8", "");
        }
    }
}
