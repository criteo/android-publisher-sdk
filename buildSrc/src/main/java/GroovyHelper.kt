import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler

class GroovyHelper() {

  fun sdkPublicationVersion(project: Project) = project.sdkPublicationVersion()

  fun addDefaultInputRepository(project: Project) = project.addDefaultInputRepository()

  fun addDefaultInputRepository(scriptHandler: ScriptHandler) = scriptHandler.addDefaultInputRepository()

  fun addAzureRepository(project: Project) = project.addAzureRepository()

  fun androidAppModule(project: Project, applicationId: String) = project.androidAppModule(applicationId)

  fun androidLibModule(project: Project) = project.androidLibModule()

}