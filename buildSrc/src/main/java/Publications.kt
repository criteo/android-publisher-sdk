import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

fun Project.addPublication(name: String, publication: MavenPublication.() -> Unit) {
  afterEvaluate {
    publishing {
      publications {
        create(name, MavenPublication::class.java) {
          version = project.sdkPublicationVersion()
          publication(this)
        }
      }
    }
  }
}