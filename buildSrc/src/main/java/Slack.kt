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

import com.slack.api.model.block.composition.BlockCompositions.markdownText
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.withType

fun Project.addSlackDeploymentMessages() {
  afterEvaluate {
    publishing {
      repositories.withType<MavenArtifactRepository> {
        val repository = this
        publications.withType<MavenPublication> {
          addSlackDeploymentMessage(this, repository)
        }
      }
    }
  }
}

private fun Project.addSlackDeploymentMessage(publication: MavenPublication, repository: MavenArtifactRepository) {
  val webHookUrl = System.getenv("SLACK_WEBHOOK") ?: return
  val teamChannel = "#direct-pub-data-releases"
  val rcChannel = "#pub-sdk-release-candidates"
  val confluenceSpaceUrl = "https://go.crto.in/publisher-sdk-bugfest"

  slack {
    messages {
      register("${publication.name}DeployedTo${repository.name.capitalize()}") {
        webHook.set(webHookUrl)

        payload {
          channel = if (isSnapshot()) {
            rcChannel
          } else {
            teamChannel
          }
          username = "Android Release"
          iconEmoji = ":android:"
        }

        publication {
          publicName.set("PublisherSDK")
          publication(publication)
          repository(repository)
        }

        if (isSnapshot()) {
          git()
        }

        changelog {
          version.set(sdkVersion())
          versionLinesStartWith("# Version")

          if (isSnapshot()) {
            versionLinesStartWith("# Next version")

            // On snapshots, we show the changelog in a code block so we can copy/paste it.
            format {
              section {
                text = markdownText("```${changelog.get()}```")
              }
            }
          }
        }

        if (isSnapshot()) {
          git {
            format {
              context {
                val gitCommand = "git fetch origin ${lastCommitSha1()}" +
                    " && git checkout FETCH_HEAD" +
                    " && git switch -c v${sdkVersion()}" +
                    " && git push"

                markdown(
                    """
*Promote as a RC*
- Go on <$confluenceSpaceUrl/Bugfest+process|Bugfest creation page> and insert `${publication.version}` as RC name
- Run `$gitCommand`
- Share this message on $teamChannel
*Validate the RC*
- Install the <${testAppUrl(publication.version)}|TestApp APK>
- Go on <$confluenceSpaceUrl/Bugfest+Android+${publication.version}|Bugfest page> and execute tests
*Release the RC*
- Create a <https://github.com/criteo/android-publisher-sdk/releases/new|new GitHub release> and insert:
  - Tag version, target, Release title: `v${sdkVersion()}`
  - Description: the changelog above
""".trimIndent()
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun testAppUrl(version: String): String {
  // FIXME this is not a stable solution, if the test app coordinates are changed, this will be
  //  broken. A proper solution could be to get the publication from the test-app module, but it may
  //  be extracted outside this project.
  //  But coordinates are pretty constant, so this is not a big deal for now.
  return "https://oss.sonatype.org/service/local/repo_groups/public/content/com/criteo/publisher/criteo-publisher-sdk-test-app/$version/criteo-publisher-sdk-test-app-$version-staging.apk"
}