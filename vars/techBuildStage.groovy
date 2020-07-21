import de.testbirds.PipelineConfig;

def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	def rtMaven = config.configuration.rtMaven
	def artifactoryServer = config.configuration.artifactoryServer
	def pomToBuild = config.configuration.pom
	def mvnGoalsBuild = config.configuration.mvnGoalsBuild
	def javaTool = config.configuration.javaTool
	def javaBootstrapTool =  config.configuration.javaBootstrapTool
	def enableArtifactory = config.configuration.enableArtifactory
	def deployArtifacts = config.configuration.deployArtifacts
	def triggerStage = config.configuration.triggerStage

	def stageName = 'build'

	if (config.stageName) {
		stageName = config.stageName
	}
	stage(stageName) {
		java = tool javaTool
		javaBootstrap = tool javaBootstrapTool

		withCredentials([
			string(credentialsId: '3f1d3330-e9f3-44a5-bcd2-117e7e4c3a92', variable: 'GPG_PASSPHRASE')
		]) {
			rtMaven.opts = "-Dmaven.repo.local=.m2/repository -Dorg.slf4j.simpleLogger.warnLevelString=WARNING -Dorg.slf4j.simpleLogger.showThreadName=false -Dorg.slf4j.simpleLogger.levelInBrackets=true -Djava.awt.headless=true -Dgpg.passphrase=${env.GPG_PASSPHRASE} -DjavaHome=" + javaBootstrap
		}

		withEnv([
			"PATH+JAVA=${java}/jre/bin",
			"JAVA_HOME=${java}"
		]) {
			if (enableArtifactory) {
				rtMaven.deployer.deployArtifacts = deployArtifacts
				def buildInfo = rtMaven.run pom: pomToBuild, goals: mvnGoalsBuild
				buildInfo.retention maxDays: 90
				if(deployArtifacts) {
					artifactoryServer.publishBuildInfo buildInfo
				}
				rtMaven.deployer.deployArtifacts = false
			} else {
				maven = tool rtMaven.tool
				sh maven + '/bin/mvn ' + rtMaven.opts + ' -f ' + pomToBuild + ' ' + mvnGoalsBuild
			}
		}

		// trigger upstream projects if this is the trigger stage
		if (triggerStage == stageName) {
			trigger.triggerDependencies triggerStage
		}
	}
}
