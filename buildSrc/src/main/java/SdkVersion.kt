import org.gradle.api.Project
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val sdkBaseVersion = "3.5.0"

private val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmm"))

fun Project.sdkVersion(): String {
  val overriddenVersion = properties["pubSdkVersion"] as? String
  return overriddenVersion ?: sdkBaseVersion
}

fun Project.sdkPublicationVersion(): String {
  val sdkVersion = sdkVersion()
  return if (properties["appendTimestamp"] == "true") {
    "$sdkVersion-$timestamp"
  } else {
    sdkVersion
  }

}