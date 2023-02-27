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
package com.criteo.publisher.activity

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import com.criteo.publisher.annotation.OpenForTesting
import com.criteo.publisher.util.PreconditionsUtil
import java.lang.ref.WeakReference

@OpenForTesting
class TopActivityFinder(private val context: Context) {

  private var topActivityRef = WeakReference<Activity?>(null)

  @Suppress("ReturnCount")
  fun getTopActivityName(): ComponentName? {
    val topActivity = topActivityRef.get()
    if (topActivity != null) {
      return topActivity.componentName
    }

    // Else we fallback on reading running tasks. ActivityManager.getRunningTasks is deprecated
    // since Lollipop, but for backward compatibility, the method still returns information on
    // owned activities, which is what we look for.
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    if (!PreconditionsUtil.isNotNull(am)) {
      return null
    }
    val taskInfo = try {
      am.getRunningTasks(1)
    } catch (e: SecurityException) {
      PreconditionsUtil.throwOrLog(e)
      null
    }
    if (taskInfo.isNullOrEmpty()) {
      return null
    }

    // The getRunningTasks may return information about an activity that the host application does
    // not own, but that is safe to share. This is the cases for launchers. So we need to filter on
    // activities that looks like our own ones.
    val topActivityName = taskInfo.firstOrNull()?.topActivity
    return if (topActivityName?.packageName?.startsWith(context.packageName) == true) {
      topActivityName
    } else null
  }

  fun registerActivityLifecycleFor(application: Application) {
    application.registerActivityLifecycleCallbacks(object : NoOpActivityLifecycleCallbacks() {
      override fun onActivityResumed(activity: Activity) {
        topActivityRef = WeakReference(activity)
      }

      override fun onActivityPaused(activity: Activity) {
        if (activity == topActivityRef.get()) {
          topActivityRef = WeakReference(null)
        }
      }
    })
  }
}
