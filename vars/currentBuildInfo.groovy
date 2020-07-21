def wasStartedByUser() {
	currentBuild.rawBuild.getCause(hudson.model.Cause$UserIdCause) != null
}

def getBuildCauses() {
	currentBuild.rawBuild.getCauses()
}
