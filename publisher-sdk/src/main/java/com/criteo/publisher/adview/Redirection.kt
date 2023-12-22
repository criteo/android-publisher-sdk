/*
 *    Copyright 2020 Criteo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.criteo.publisher.adview

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.criteo.publisher.activity.NoOpActivityLifecycleCallbacks
import com.criteo.publisher.annotation.OpenForTesting

@OpenForTesting
class Redirection(private val context: Context) {
  fun redirect(
      uri: String,
      hostActivityName: ComponentName?,
      listener: RedirectionListener
  ) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    // this gets called after the user has clicked on the creative. In case of deeplink,
    // if the target application is not installed on the device, an ActivityNotFoundException
    // will be thrown. Therefore, an explicit check is made to ensure that there exists at least
    // one package that can handle the intent
    val packageManager = context.packageManager
    val list = packageManager.queryIntentActivities(
        intent, PackageManager.MATCH_DEFAULT_ONLY
    )
    if (list.size > 0) {
      context.startActivity(intent)
      listener.onUserRedirectedToAd()
      if (hostActivityName != null) {
        val application = context.applicationContext as Application
        val tracker = BackOnTargetActivityTracker(
            application,
            hostActivityName,
            listener
        )
        application.registerActivityLifecycleCallbacks(tracker)
      }
    } else {
      listener.onRedirectionFailed()
    }
  }

  private class BackOnTargetActivityTracker(
      private val application: Application,
      private val trackedActivity: ComponentName,
      private var listener: RedirectionListener?
  ) : NoOpActivityLifecycleCallbacks() {
    override fun onActivityResumed(activity: Activity) {
      if (trackedActivity != activity.componentName) {
        return
      }
      val listener = listener ?: return
      listener.onUserBackFromAd()
      application.unregisterActivityLifecycleCallbacks(this)
      this.listener = null
    }
  }
}
