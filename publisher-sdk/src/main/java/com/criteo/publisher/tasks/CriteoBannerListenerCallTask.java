package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoListenerCode;
import java.lang.ref.WeakReference;

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

    private CriteoBannerAdListener adListener;
    private WeakReference<CriteoBannerView> view;

    /**
     * Task that calls the relevant callback in the criteoBannerAdListener based on the CriteoListenerCode passed to
     * execute. Passes the criteoBannerView as a parameter to the onAdReceived callback if the CriteoListenerCode is
     * valid
     */
    public CriteoBannerListenerCallTask(CriteoBannerAdListener criteoBannerAdListener
            , CriteoBannerView criteoBannerView) {
        adListener = criteoBannerAdListener;
        this.view = new WeakReference<>(criteoBannerView);
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
        if (adListener != null) {
            if (object == null) {
                adListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
            } else {
                CriteoListenerCode code = (CriteoListenerCode) object;
                switch (code) {
                    case INVALID:
                        adListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
                        break;
                    case VALID:
                        adListener.onAdReceived(view.get());
                        break;
                    case CLICK:
                        adListener.onAdClicked();
                        adListener.onAdLeftApplication();
                        adListener.onAdOpened();
                        break;
                }
            }
        }
    }
}
