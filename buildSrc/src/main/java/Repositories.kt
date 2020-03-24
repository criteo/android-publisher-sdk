import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

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
}

internal fun Project.addNexusRepositories() {
  publishing {
    addNexusProdRepository()
    addNexusPreProdRepository()
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

internal fun Project.addAzureRepository() {
  val localRepository = "${project.buildDir}/azure-${project.sdkPublicationVersion()}"

  project.afterEvaluate {
    publishing {
      repositories {
        maven {
          // TODO EE-915 Find a way to make the upload from Gradle rather than relying on bash script
          name = "azure"
          setUrl("file://$localRepository")
        }
      }
    }
  }

  val uploadToAzure by project.tasks.registering(Exec::class) {
    val scriptPath = project.rootDir.toPath()
        .resolve("./scripts/azureDeploy.sh")
        .toAbsolutePath()
        .toString()

    commandLine(
        "bash",
        scriptPath,
        project.sdkPublicationVersion(),
        localRepository)
  }

  project.tasks.withType(PublishToMavenRepository::class.java).all {
    if (name.contains("Azure")) {
      finalizedBy(uploadToAzure)
    }
  }
}