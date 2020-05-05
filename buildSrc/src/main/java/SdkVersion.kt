import org.gradle.api.Project
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val sdkBaseVersion = "3.5.0"

private val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmm"))

fun sdkVersion(): String {
  return sdkBaseVersion
}

fun Project.sdkPublicationVersion(): String {
  val sdkVersion = sdkVersion()
  return if (properties["appendTimestamp"] == "true") {
    "$sdkVersion-$timestamp"
  } else {
    sdkVersion
  }

}