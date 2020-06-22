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
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.withType

fun Project.addSlackDeploymentMessages() {
  val webHookUrl = System.getenv("SLACK_WEBHOOK") ?: return
  val teamChannel = "#pub-sdk-private"
  val rcChannel = "#pub-sdk-release-candidates"
  val confluenceSpaceUrl = "https://confluence.criteois.com/display/PUBSDK/"
  val gerritProjectBranchesUrl = "http://review.crto.in/#/admin/projects/pub-sdk/mochi,branches"

  afterEvaluate {
    tasks.withType<PublishToMavenRepository>()
        .matching { it.repository.isAzure() || it.repository.isNexusProd() }
        .all {
      slack {
        messages {
          register("${publication.name}DeployedTo${repository.name.capitalize()}") {
            webHook.set(webHookUrl)

            payload {
              channel = if (repository.isAzure()) {
                teamChannel
              } else {
                rcChannel
              }
              username = "Android Release"
              iconEmoji = ":android:"
            }

            publication {
              publicName.set("PublisherSDK")
              publication(publication)
              repository(repository)
            }

            git()

            changelog {
              version.set(sdkVersion())
              versionLinesStartWith("# Version")
            }

            if (repository.isNexusProd()) {
              git {
                format {
                  context {
                    markdown("""
*Promote as a RC*
- Go on <$confluenceSpaceUrl/Bugfest+process|Bugfest creation page> and insert `${publication.version}` as RC name
- Go on <$gerritProjectBranchesUrl|Gerrit> and create the `v${sdkVersion()}` branch on this commit SHA-1 (only for new version)
- Share this message on $teamChannel
*Validate the RC*
- Install the <${testAppUrl(publication.version)}|TestApp APK>
- Go on <$confluenceSpaceUrl/Bugfest+Android+${publication.version}|Bugfest page> and execute tests
*Release the RC*
- Go on <https://build.crto.in/job/pub-sdk-mochi-prod-deployment/build|Jenkins deploy job> and insert this commit SHA-1
""".trimIndent())
                  }
                }
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
  return "http://nexus.criteo.prod/content/groups/android/com/criteo/pubsdk_android/publisher-app/$version/publisher-app-$version-staging.apk"
}