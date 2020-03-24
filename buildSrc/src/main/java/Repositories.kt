import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.publish.PublishingExtension

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

internal fun PublishingExtension.addNexusRepositories() {
    addNexusProdRepository()
    addNexusPreProdRepository()
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