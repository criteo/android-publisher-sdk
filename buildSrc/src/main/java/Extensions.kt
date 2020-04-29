import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import fr.pturpin.slackpublish.SlackPublishExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension

internal val Project.androidBase: BaseExtension get() =
  (this as ExtensionAware).extensions.getByName("android") as BaseExtension

internal fun Project.androidBase(configure: BaseExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("android", configure)

internal fun Project.androidApp(configure: AppExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("android", configure)

internal fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("publishing", configure)

internal fun Project.hasPublishing(): Boolean =
    hasProperty("publishing")

internal fun Project.slack(configure: SlackPublishExtension.() -> Unit): Unit =
  (this as ExtensionAware).extensions.configure("slack", configure)
