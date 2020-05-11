import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.kotlin.dsl.withType

fun Project.addSlackDeploymentMessages() {
  val webHookUrl = System.getenv("SLACK_WEBHOOK") ?: return
  val teamChannel = "#pub-sdk-private"
  val rcChannel = "#pub-sdk-release-candidates"
  val confluenceSpaceUrl = "https://confluence.criteois.com/display/PUBSDK/"

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

            if (repository.isNexusProd()) {
              git {
                format {
                  context {
                    markdown("""
*Promote as a RC*
- Go on <$confluenceSpaceUrl/Bugfest+process|Bugfest creation page> and insert `${publication.version}` as RC name
- Share this message on $teamChannel
*Validate the RC*
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