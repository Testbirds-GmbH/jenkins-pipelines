import de.testbirds.PipelineConfig;

def call(body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

	def url = config.configuration.url
	def triggerStage = config.configuration.triggerStage

	def stageName = 'checkout'

	if (config.stageName) {
		stageName = config.stageName
	}
	stage(stageName) {
		if(url) {
			// determine default branch if branch is unset
			def branch = 'master'
			if(config.configuration.branch) {
				branch = config.configuration.branch
			}

			// checkout
			git branch: branch, credentialsId: 'b40bb9ff-db75-4385-bf14-db5752bf94bf', url: url
		} else {
			// for multibranch projects the url should be set to null
			checkout scm
		}

		// trigger upstream projects if this is the trigger stage
		if (triggerStage == stageName) {
			trigger.triggerDependencies triggerStage
		}
	}
}
