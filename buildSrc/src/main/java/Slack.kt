import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.withType

fun Project.addSlackDeploymentMessages() {
  val webHookUrl = System.getenv("SLACK_WEBHOOK") ?: return

  afterEvaluate {
    tasks.withType<PublishToMavenRepository>()
        .matching { it.repository.isAzure() || it.repository.isNexusProd() }
        .all {
      slack {
        messages {
          register("${publication.name}DeployedTo${repository.name.capitalize()}") {
            webHook.set(webHookUrl)

            payload {
              if (repository.isAzure()) {
                channel = "#pub-sdk-private"
              } else {
                channel = "#pub-sdk-release-candidates"
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

            if (repository.isNexusProd()) {
              git {
                format {
                  context {
                    markdown("How-to release: Go on " +
                        "<https://build.crto.in/job/pub-sdk-mochi-prod-deployment/build|Jenkins deploy job> " +
                        "and insert this commit SHA-1"
                    )
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