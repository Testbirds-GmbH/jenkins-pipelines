def loadPipeline(String pipelineBranch) {
	log.info "Loading multibranch pipeline project in path " + JOB_NAME  + " with branch " + env.BRANCH_NAME

	node('master') {
		log.info "Loading pipeline scripts from branch " + pipelineBranch
		git branch: pipelineBranch, credentialsId: 'b40bb9ff-db75-4385-bf14-db5752bf94bf', url: 'git@git.testbirds.com:tech/jenkins-pipelines.git'

		// Loads the actual job pipeline from the shared library
		// JOB_NAME from the env return the path like "/folder/project/branch"
		def path = "pipelines/" + JOB_NAME
		def pathReal = path.replace("%2F", "/")

		// fallback path uses multibranch file instead of branch name
		def fallbackPath = path.substring(0, path.lastIndexOf("/")) + "/multibranch"
		def fallbackPathReal = fallbackPath.replace("%2F", "/")

		if (fileExists(pathReal)) {
			log.info "Loading script " + pathReal
			load pathReal
		} else if(fileExists(fallbackPathReal)){
			log.info "Loading multibranch script " + fallbackPathReal
			load fallbackPathReal
		} else {
			echo "ERROR: Could not load pipeline script. Job name: " + JOB_NAME
			throw new FileNotFoundException("Neither branch nor fallback pipeline found")
		}
	}
}
