import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get

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

fun Project.createSourcesJarTask(variant: String): Jar {
  return tasks.create("${variant}SourcesJar", Jar::class.java) {
    archiveClassifier.set("sources")
    from(androidBase.sourceSets["main"].java.srcDirs)
    from(androidBase.sourceSets[variant].java.srcDirs)
  }
}