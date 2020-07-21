import de.testbirds.PipelineConfig;

def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	def rtMaven = config.configuration.rtMaven
	def artifactoryServer = config.configuration.artifactoryServer
	def pomToBuild = config.configuration.pom
	def mvnModules = config.configuration.mvnModules
	def mvnGoalsAnalyze = config.configuration.mvnGoalsAnalyze
	def javaTool = config.configuration.javaTool
	def javaBootstrapTool = config.configuration.javaBootstrapTool
	def jacocoExclusions = config.configuration.jacocoExclusions
	def sonarqube = config.configuration.sonarqube
	def enableArtifactory = config.configuration.enableArtifactory
	def triggerStage = config.configuration.triggerStage

	def stageName = 'analyze'

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
				if (sonarqube) {
					withSonarQubeEnv(sonarqube) {
						rtMaven.run pom: pomToBuild, goals: mvnGoalsAnalyze + ' sonar:sonar'
					}
				} else {
					rtMaven.run pom: pomToBuild, goals: mvnGoalsAnalyze
				}
			} else {
				maven = tool rtMaven.tool
				sh maven + '/bin/mvn ' + rtMaven.opts + ' -f ' + pomToBuild + ' ' + mvnGoalsAnalyze
			}
		}

		// JUnit results
		// TODO: logs as JUnit attachments
		junit keepLongStdio: true, allowEmptyResults: true, testDataPublishers: [
			[$class: 'AttachmentPublisher'],
			[$class: 'StabilityTestDataPublisher']
		], testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml'

		// jacoco coverage
		try {
			jacoco sourcePattern: '**/src/main/java,**/src/main/scala', exclusionPattern: jacocoExclusions
		} catch(Exception e) {
			log.warn "Jacoco analysis failed (possibly no results): " + e.getMessage()
		}

		// analysis plugins
		recordIssues(tools: [taskScanner(excludePattern: '**/target/**', includePattern: '**/*.java, **/*.scala, **/*.xml', highTags: 'FIXME', lowTags: 'TODO', normalTags: 'DIRTY, HACK'), checkStyle(pattern: '**/target/checkstyle-result.xml, **/target/scalastyle-result.xml', reportEncoding: 'UTF-8'), pmdParser(pattern: '**/target/pmd.xml', reportEncoding: 'UTF-8'), cpd(pattern: '**/target/cpd.xml', reportEncoding: 'UTF-8'), spotBugs(pattern: '**/target/spotbugsXml.xml', reportEncoding: 'UTF-8')])

		recordIssues enabledForFailure: true, tools: [mavenConsole(reportEncoding: 'UTF-8'), java(reportEncoding: 'UTF-8'),	javaDoc(reportEncoding: 'UTF-8'), scala(reportEncoding: 'UTF-8')]

		// maven sites per module
		if (mvnModules) {
			sh 'rm -rf sites && mkdir -p sites'
			sh 'for i in $( echo */target/site ) ; do cp -r "${i}" "sites/${i%%/*}" ; done'
			reportFiles = mvnModules.split(",").collect { it + "/index.html" }.join(',')
			publishHTML([keepAll: true, reportDir: 'sites', reportName: 'Maven Sites', reportFiles: reportFiles, reportTitles: mvnModules])
		}

		// archive all relevant artifacts (excluding what is handled by artifactory)
		archiveArtifacts allowEmptyArchive: true, artifacts: '**/target/*.log, **/target/jsondoc-ui/*', fingerprint: true

		// trigger upstream projects if this is the trigger stage
		if (triggerStage == stageName) {
			trigger.triggerDependencies triggerStage
		}
	}
}
