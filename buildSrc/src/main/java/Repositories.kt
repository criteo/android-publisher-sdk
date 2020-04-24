import azure.AzureBlobStorage
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import java.nio.file.Paths

fun Project.addDefaultInputRepository() {
  repositories.addDefaultInputRepository()
}

fun ScriptHandler.addDefaultInputRepository() {
  repositories.addDefaultInputRepository()
}

internal fun RepositoryHandler.addDefaultInputRepository() {
  google()
  jcenter()
  maven {
    setUrl("https://s3.amazonaws.com/moat-sdk-builds")
  }
  maven {
    setUrl("http://nexus.criteo.prod/content/groups/android/")
  }
}

/**
 * Add a local repository in the build directory for development and testing purpose.
 * Compared to the maven local repository, this has the advantage to be cleaned by cleaning tasks
 * and has no side effect on other working directory.
 */
internal fun Project.addDevRepository() {
  val devRepository = "${project.buildDir}/dev-${project.sdkPublicationVersion()}"

  publishing {
    repositories {
      maven {
        name = "Dev"
        setUrl("file://$devRepository")
      }
    }
  }
}

internal fun PublishingExtension.addNexusPreProdRepository() {
  repositories {
    maven {
      name = "NexusPreProd"
      setUrl("http://nexus.criteo.preprod/content/repositories/criteo.android.releases/")
      withMavenCredentialsIfPresent()
    }
  }
}

internal fun PublishingExtension.addNexusProdRepository() {
  repositories {
    maven {
      name = "NexusProd"
      setUrl("http://nexus.criteo.prod/content/repositories/criteo.android.releases/")
      withMavenCredentialsIfPresent()
    }
  }
}

private fun MavenArtifactRepository.withMavenCredentialsIfPresent() {
  if (System.getenv("MAVEN_USER") != null) {
    credentials {
      username = System.getenv("MAVEN_USER")
      password = System.getenv("MAVEN_PASSWORD")
    }
  }
}

fun Project.addAzureRepository() {
  val blobStorage = AzureBlobStorage(containerName = "publishersdk") {
    credentials {
      // TODO EE-926 Retrieve azure credentials from a vault
      username = "pubsdkuseprod"
      password = "IBXkbamPEDzFFvLFgjL8bG5v7GOLy/2HY2xMVtgXICxSXG/AYYP57Xme9lxNgcoaznc2XGdye/zDT7fPUYrXbA=="
    }
  }

  val localRepository = Paths.get("${buildDir}/azure-${sdkPublicationVersion()}/android")

  publishing {
    repositories {
      maven {
        name = "azure"
        setUrl(localRepository)
      }
    }
  }

  fun TaskContainer.registerDownloadMavenMetadataTask(publication: MavenPublication): TaskProvider<Task> {
    val groupIdPath = publication.groupId.replace('.', '/')
    val artifactId = publication.artifactId
    val relativePath = "$groupIdPath/$artifactId/maven-metadata.xml"
    val path = localRepository.resolve(relativePath)
    val blobName = "android/$relativePath"

    return register("download${publication.name.capitalize()}MavenMetadataFromAzureRepository") {
      group = "publishing"
      description = "Download the maven-metadata.xml file to keep version history"
      outputs.file(path)

      doLast {
        blobStorage.download(blobName, path)
      }
    }
  }

  fun TaskContainer.registerUploadToAzureTask(publication: MavenPublication): TaskProvider<Task> {
    return register("upload${publication.name.capitalize()}ToAzureRepository") {
      group = "publishing"
      description = "Upload the local maven repository to the Azure repository"
      inputs.dir(localRepository)

      doLast {
        blobStorage.upload(localRepository)
      }
    }
  }

  tasks.withType(PublishToMavenRepository::class.java).all {
    if (name.contains("Azure")) {
      afterEvaluate {
        dependsOn(tasks.registerDownloadMavenMetadataTask(publication))
        finalizedBy(tasks.registerUploadToAzureTask(publication))
      }
    }
  }
}