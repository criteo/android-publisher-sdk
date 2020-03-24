import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.publish.maven.MavenPublication

class GroovyHelper() {

  fun addDefaultInputRepository(project: Project) = project.addDefaultInputRepository()

  fun addDefaultInputRepository(scriptHandler: ScriptHandler) = scriptHandler.addDefaultInputRepository()

  fun addAzureRepository(project: Project) = project.addAzureRepository()

  fun androidAppModule(project: Project, applicationId: String) = project.androidAppModule(applicationId)

  fun androidLibModule(project: Project) = project.androidLibModule()

  fun addPublication(project: Project, name: String, closure: Closure<MavenPublication>) = project.addPublication(name, closure::call)

}