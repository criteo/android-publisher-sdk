import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler

class GroovyHelper() {

  fun sdkVersion(project: Project) = project.sdkVersion()

  fun sdkPublicationVersion(project: Project) = project.sdkPublicationVersion()

  fun addDefaultInputRepository(project: Project) = project.addDefaultInputRepository()

  fun addDefaultInputRepository(scriptHandler: ScriptHandler) = scriptHandler.addDefaultInputRepository()

  fun addNexusRepositories(project: Project) = project.addNexusRepositories()

  fun addAzureRepository(project: Project) = project.addAzureRepository()

}