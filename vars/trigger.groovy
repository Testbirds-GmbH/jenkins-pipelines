import de.testbirds.JobGrid;
import de.testbirds.JobNode;

def triggerDependencies(String stage) {
	log.info "Triggering upstream projects in stage " + stage
	JobGrid grid = new JobGrid();
	def res = libraryResource 'dependencies.txt'
	grid.loadDependencies(res)
	for (String dependency : grid.getProjectsToTrigger(JOB_NAME)) {
		try {
			build job: dependency, wait: false, propagate: false
		} catch(Exception e) {
			log.warn "Failed to trigger dependency " + dependency
		}
	}
}

/**
 * Triggers the specified project if this build was not triggered by a user.
 */
def triggerIfNotStartedByUser(String project) {
	if (currentBuildInfo.wasStartedByUser()) {
		log.info "Build was started by an user. Skipping trigger queue..."
	} else {
		log.info "Triggering " + project
		build job: project, wait: false
	}
}
