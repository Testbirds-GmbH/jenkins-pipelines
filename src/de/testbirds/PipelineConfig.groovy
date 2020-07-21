package de.testbirds;

class PipelineConfig implements Serializable{

	def script
	def artifactoryServer
	def rtMaven

	String pom = 'pom.xml'
	String url
	String branch
	String mvnModules
	String mvnGoalsBuild = '--batch-mode --show-version --fail-at-end --update-snapshots -Dmaven.test.skip=true clean install'
	String mvnGoalsAnalyze = '--batch-mode --fail-at-end verify site'
	String javaTool = 'Java 8'
	String javaBootstrapTool
	String mvnTool = 'Maven 3.5'
	String gradleTool = 'Gradle 3.4'
	String resReleaseRepo = 'all-repos'
	String resSnapshotRepo = 'all-repos'
	String depReleaseRepo = 'libs-release-local'
	String depSnapshotRepo = 'libs-snapshot-local'
	String jacocoExclusions = ''
	String sonarqube
	// the root dir which contains the package.json for a node/npm or gradle
	String rootDir
	Boolean enableArtifactory = true
	Boolean deployArtifacts = true
	String triggerStage = 'build'

	def configure() {
		if (!artifactoryServer && !rtMaven) {
			artifactoryServer = script.Artifactory.server 'TestChameleon Artifactory'
			rtMaven = script.Artifactory.newMavenBuild()
			rtMaven.resolver server: artifactoryServer, releaseRepo: resReleaseRepo, snapshotRepo: resSnapshotRepo
			rtMaven.deployer server: artifactoryServer, releaseRepo: depReleaseRepo, snapshotRepo: depSnapshotRepo
			rtMaven.tool = mvnTool
			rtMaven.deployer.deployArtifacts = false
		}

		// make sure the bootstrap tool is set
		if(!javaBootstrapTool) {
			javaBootstrapTool = javaTool
		}
	}
}
