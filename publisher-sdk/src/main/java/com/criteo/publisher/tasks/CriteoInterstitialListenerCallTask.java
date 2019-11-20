package com.criteo.publisher.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.model.Slot;
import com.criteo.publisher.model.TokenValue;

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
public class CriteoInterstitialListenerCallTask extends AsyncTask<Object, Void, Object> {

    private static final String TAG = "Criteo.ILCT";

    private CriteoInterstitialAdListener criteoInterstitialAdListener;

    public CriteoInterstitialListenerCallTask(CriteoInterstitialAdListener listener) {
        this.criteoInterstitialAdListener = listener;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Object result = null;

        try {
            result = doInterstitialListenerCallTask(objects);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal ILCT exec error.", tr);
        }

        return result;
    }

    private Object doInterstitialListenerCallTask(Object... objects) {
        if (objects == null || objects.length == 0) {
            return null;
        }
        Object object = objects[0];
        return object;
    }

    @Override
    protected void onPostExecute(Object object) {
        try {
            doOnPostExecute(object);
        } catch (Throwable tr) {
            Log.e(TAG, "Internal ILCT PostExec error.", tr);
        }
    }

    private void doOnPostExecute(Object object) {
        super.onPostExecute(object);
        if (criteoInterstitialAdListener != null) {
            if (object == null) {
                criteoInterstitialAdListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
                return;
            }

            if (object instanceof Slot) {
                Slot slot = (Slot) object;
                if (!slot.isValid() || !URLUtil
                        .isValidUrl(slot.getDisplayUrl())) {
                    criteoInterstitialAdListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
                } else {
                    criteoInterstitialAdListener.onAdReceived();
                }
            } else if (object instanceof TokenValue) {
                TokenValue tokenValue = (TokenValue) object;
                if (!URLUtil.isValidUrl(tokenValue.getDisplayUrl())) {
                    criteoInterstitialAdListener.onAdFailedToReceive(CriteoErrorCode.ERROR_CODE_NO_FILL);
                } else {
                    criteoInterstitialAdListener.onAdReceived();
                }
            }
        }
    }
}
