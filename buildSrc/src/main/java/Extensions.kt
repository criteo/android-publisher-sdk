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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import fr.pturpin.slackpublish.SlackPublishExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension

internal val Project.androidBase: BaseExtension get() =
  (this as ExtensionAware).extensions.getByName("android") as BaseExtension

internal val Project.publishing: PublishingExtension get() =
  (this as ExtensionAware).extensions.getByName("publishing") as PublishingExtension

internal fun Project.androidBase(configure: BaseExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("android", configure)

internal fun Project.androidApp(configure: AppExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("android", configure)

internal fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("publishing", configure)

internal fun Project.hasPublishing(): Boolean =
    (this as ExtensionAware).extensions.findByName("publishing") != null

internal fun Project.slack(configure: SlackPublishExtension.() -> Unit): Unit =
  (this as ExtensionAware).extensions.configure("slack", configure)
