package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoListenerCode;
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
public class CriteoBannerListenerCallTask extends AsyncTask<Object, Void, Object> {

    @Nullable
    private CriteoBannerAdListener listener;

    @NonNull
    private Reference<CriteoBannerView> bannerViewRef;

    /**
     * Task that calls the relevant callback in the {@link CriteoBannerAdListener} based on the
     * {@link CriteoListenerCode} passed to execute. Passes the {@link CriteoBannerView} as a
     * parameter to the onAdReceived callback if the CriteoListenerCode is valid.
     */
    public CriteoBannerListenerCallTask(
        @Nullable CriteoBannerAdListener listener,
        @NonNull Reference<CriteoBannerView> bannerViewRef) {
        this.listener = listener;
        this.bannerViewRef = bannerViewRef;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }
        return objects[0];
    }

    @Override
    protected void onPostExecute(Object object) {
        if (listener != null) {
            if (object == null) {
                listener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
            } else {
                CriteoListenerCode code = (CriteoListenerCode) object;
                switch (code) {
                    case INVALID:
                        listener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
                        break;
                    case VALID:
                        listener.onAdReceived(bannerViewRef.get());
                        break;
                    case CLICK:
                        listener.onAdClicked();
                        listener.onAdLeftApplication();
                        listener.onAdOpened();
                        break;
                }
            }
        }
    }
}
