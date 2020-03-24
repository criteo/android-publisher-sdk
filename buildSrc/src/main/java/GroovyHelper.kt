import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.publish.PublishingExtension

class GroovyHelper() {

  fun sdkVersion(project: Project) = project.sdkVersion()

  fun sdkPublicationVersion(project: Project) = project.sdkPublicationVersion()

  fun addDefaultInputRepository(project: Project) = project.addDefaultInputRepository()

  fun addDefaultInputRepository(scriptHandler: ScriptHandler) = scriptHandler.addDefaultInputRepository()

  fun addNexusRepositories(publishing: PublishingExtension) = publishing.addNexusRepositories()

  fun addNexusPreProdRepository(publishing: PublishingExtension) = publishing.addNexusPreProdRepository()

  fun addNexusProdRepository(publishing: PublishingExtension) = publishing.addNexusProdRepository()

}