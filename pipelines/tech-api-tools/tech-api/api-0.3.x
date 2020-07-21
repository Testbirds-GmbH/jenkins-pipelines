@Library('tech-pipeline@develop') _
import de.testbirds.PipelineConfig

properties([
	buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '40', numToKeepStr: '')),
	gitLabConnection('Testbirds Gitlab'),
	pipelineTriggers([
		[$class: 'GitLabPushTrigger', branchFilterType: "NameBasedFilter", includeBranchesSpec: "api-0.3.x", triggerOnPush: true, triggerOnMergeRequest: false, triggerOnNoteRequest: false],
		pollSCM('H/10 * * * *')
		])
	])

techpipeline.pipe('master') {
	def techConfig = new PipelineConfig([script:this,
		pom:'tech-parent/pom.xml',
		url: 'git@git.testbirds.com:tech/tech-api.git',
		branch: 'api-0.3.x',
		mvnModules: 'tech-parent,tech-api,testcase-api,java6-compliance,olddeps-compliance',
		mvnTool: 'Maven 3.3',
		javaBootstrapTool: 'Java 6',
		jacocoExclusions: '**/de/testbirds/tech/api/data/**/*.class,**/de/testbirds/tech/api/event/decorator/**/*.class,**/de/testbirds/tech/api/event/info/**/*.class,**/de/testbirds/tech/api/event/jwt/**/*.class,**/de/testbirds/tech/api/event/service/**/*.class,**/de/testbirds/tech/api/event/startup/**/*.class,**/de/testbirds/tech/api/event/system/**/*.class,**/de/testbirds/tech/api/event/vm/**/*.class,**/de/testbirds/tech/api/event/vpn/**/*.class,**/de/testbirds/tech/api/rest/dto/**/*.class,**/de/testbirds/tech/testcase/**/*.class',
		])
	techConfig.configure()

	techCheckoutStage {
		configuration = techConfig
	}

	techBuildStage {
		configuration = techConfig
	}

	def techConfigOlddeps = new PipelineConfig([script:this,
		pom: 'olddeps-compliance/pom.xml',
		mvnGoalsBuild: '--batch-mode --fail-at-end verify site',
		mvnTool: 'Maven 3.3',
		javaBootstrapTool: 'Java 6',
		deployArtifacts: false])
	techConfigOlddeps.configure()

	techBuildStage {
		stageName = 'old dependencies test'
		configuration = techConfigOlddeps
	}

	// do the analyze in the end, collecting ALL results, also from separate ITs
	techAnalyzeStage {
		configuration = techConfig
	}
}
