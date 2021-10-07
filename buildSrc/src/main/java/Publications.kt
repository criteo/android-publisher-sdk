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

import groovy.util.NodeList
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get

object Publications {
  const val sdkDescription = "SDK of Direct Bidder for App"
  const val website = "https://publisherdocs.criteotilt.com/app/android/get-started/"
  private const val githubRepo = "android-publisher-sdk"
  const val githubUrl = "https://github.com/criteo/$githubRepo"
  const val githubReadOnlyUrl = "scm:git:git://github.com/criteo/$githubRepo.git"
  const val githubReadWriteUrl = "scm:git:ssh://github.com:criteo/$githubRepo.git"
}

fun Project.addPublication(name: String, publication: SdkPublication.() -> Unit) {
  publishing {
    publications {
      register(name, MavenPublication::class.java) {
        publication(SdkPublication(this@addPublication, this@register))
      }
    }
  }
}

class SdkPublication(
    private val project: Project,
    private val mavenPublication: MavenPublication
) : MavenPublication by mavenPublication {

  init {
    setDefaultValue()
    signPublication()
  }

  fun addSourcesJar(variantName: String) {
    artifact(project.createSourcesJarTask(variantName))
  }

  fun addJavadocJar(variantName: String) {
    project.getJavadocTask(variantName).apply {
      dependsOn(project.getAssembleTask(variantName))
      isFailOnError = false
    }

    artifact(project.getJavadocJarTask(variantName))
  }

  private fun Project.createSourcesJarTask(variant: String): Jar {
    return tasks.create("generate${variant.capitalize()}SourcesJar", Jar::class.java) {
      group = "documentation"
      description = "Generate a source JAR for $variant variant."

      archiveClassifier.set("sources")
      from(androidBase.sourceSets["main"].java.srcDirs)
      from(androidBase.sourceSets[variant].java.srcDirs)
      archiveBaseName.set("${project.name}-${androidBase.defaultConfig.versionName}-$variant")
    }
  }

  private fun Project.getAssembleTask(variant: String): Task {
    return project.tasks.getByName("assemble${variant.capitalize()}")
  }

  private fun Project.getJavadocTask(variant: String): Javadoc {
    return project.tasks.getByName("generate${variant.capitalize()}Javadoc") as Javadoc
  }

  private fun Project.getJavadocJarTask(variant: String): Jar {
    return project.tasks.getByName("generate${variant.capitalize()}JavadocJar") as Jar
  }

  private fun setDefaultValue() {
    groupId = project.rootProject.group as String
    version = project.sdkPublicationVersion()

    pom {
      withXml {
        val dependenciesNode = asNode()["dependencies"] as groovy.util.NodeList
        val dependencyNode = dependenciesNode["dependency"]
        dependencyNode.forEach {
          val node = it as groovy.util.Node
          val artifactIdNode = node["artifactId"] as groovy.util.NodeList
          if (artifactIdNode.text() == "multidex") {
            // Multidex is added for the tests so that we can use any dependencies we want. The SDK
            // in itself is not really big and we should not impose this to publishers, so lets
            // remove it.
            node.parent().remove(node)
          }
        }
      }

      name.set(project.provider { "$groupId:$artifactId" })
      url.set(Publications.website)

      licenses {
        license {
          name.set("Apache License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          distribution.set("repo")
        }
      }

      developers {
        // We rely on Git to recognize contributors
        developer {
          name.set("R&D Direct")
          email.set("rnd-direct@criteo.com")
          organization.set("Criteo")
          organizationUrl.set("https://www.criteo.com/")
        }
      }

      scm {
        url.set(Publications.githubUrl)
        connection.set(Publications.githubReadOnlyUrl)
        developerConnection.set(Publications.githubReadWriteUrl)
      }
    }
  }

  private operator fun NodeList.get(name: String): NodeList = getAt(name)

  private fun signPublication() {
    project.signing?.apply {
      val secretKey = System.getenv("MAVEN_SECRING_GPG_BASE64")
      val password = System.getenv("MAVEN_SECRING_PASSWORD")

      if (secretKey != null && password != null) {
        useInMemoryPgpKeys(secretKey, password)
        sign(mavenPublication)
      }
    }
  }

}
