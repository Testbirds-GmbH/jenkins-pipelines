def pipe(String label, Closure body) {
	if(env.NODE_NAME != label && !(env.NODE_LABELS && label in env.NODE_LABELS.split(' '))) {
		node(label) {
			pipe(label, body)
		}
	} else {
		try {
			timestamps {
				body.call()
			}
			if (currentBuild.result == "UNSTABLE") {
				emailext body: '$DEFAULT_CONTENT  ${FAILED_TESTS}', replyTo: '$DEFAULT_REPLYTO', subject: '$DEFAULT_SUBJECT ${TEST_COUNTS, var="fail"} failures!', to: 'tech@testbirds.de' , postsendScript: '$DEFAULT_POSTSEND_SCRIPT', presendScript: '$DEFAULT_PRESEND_SCRIPT'
			}
		} catch (Exception e) {
			currentBuild.result = "FAILURE"
			emailext body: '$DEFAULT_CONTENT', replyTo: '$DEFAULT_REPLYTO', subject: '$DEFAULT_SUBJECT', to: 'tech@testbirds.de' , postsendScript: '$DEFAULT_POSTSEND_SCRIPT', presendScript: '$DEFAULT_PRESEND_SCRIPT'
			// throw it!
			throw e
		}

		// TODO: configurable. Will be done when global object is configurable
	}
}
