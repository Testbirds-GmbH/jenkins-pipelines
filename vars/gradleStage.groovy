import de.testbirds.PipelineConfig;
import de.testbirds.JobGrid;
import de.testbirds.JobNode;

def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	def directory = config.configuration.rootDir
	def goal = config.goal
	def triggerStage = config.configuration.triggerStage
	def artifactoryServer = config.configuration.artifactoryServer

	def stageName = config.stageName ?: "gradle ${goal}"
	stage(stageName) {
		rtGradle = Artifactory.newGradleBuild()
		rtGradle.tool = config.configuration.gradleTool

		// TODO make configurable
		withCredentials([
			file(credentialsId: 'android-keystore', variable: 'KEYSTORE'),
			string(credentialsId: 'keystore-password', variable: 'KEYSTORE_PW'),
			string(credentialsId: 'keystore-alias', variable: 'KEY_ALIAS'),
			string(credentialsId: 'keystore-password', variable: 'KEY_PW')
		]) {
			rtGradle.run rootDir: directory, buildFile: 'build.gradle', tasks: goal
		}

		// trigger upstream projects if this is the trigger stage
		if (triggerStage == stageName) {
			trigger.triggerDependencies triggerStage
		}
	}
}
