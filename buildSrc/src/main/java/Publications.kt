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

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
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
    mavenPublication: MavenPublication
) : MavenPublication by mavenPublication {

  init {
    setDefaultValue()
  }

  fun addSourcesJar(variantName: String) {
    artifact(project.createSourcesJarTask(variantName))
  }

  fun addJavadocJar(variantName: String) {
    artifact(project.getJavadocTask(variantName))
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

  private fun Project.getJavadocTask(variant: String): Jar {
    return project.tasks.getByName("generate${variant.capitalize()}JavadocJar") as Jar
  }

  private fun setDefaultValue() {
    version = project.sdkPublicationVersion()

    pom {
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
      }

      scm {
        url.set(Publications.githubUrl)
        connection.set(Publications.githubReadOnlyUrl)
        developerConnection.set(Publications.githubReadWriteUrl)
      }
    }
  }

}
