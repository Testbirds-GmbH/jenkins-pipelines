package de.testbirds;

import de.testbirds.JobNode;

class JobGrid implements Serializable {

	/**
	 * All nodes inside the tree but not sorted
	 */
	List<JobNode> nodes = new ArrayList<>()

	/**
	 * Default constructor
	 */
	JobGrid() {
	}

	/**
	 * Adding a node to the grid. Order does not matter. Dependencies get resolved a different way
	 * @param node The actual node you want to add
	 */
	def addNode(JobNode node) {
		nodes.add(node);
	}

	/**
	 * Most common use case as we want to trigger all dependencies where have the same parentnode
	 * Be aware that there is no further checking of the name
	 * @param parentNodeName The actual name of the current node
	 * @return a List of paths
	 */
	def List<String> getProjectsToTrigger(String parentNodeName) {
		//TODO use better structure, but there are only a couple of entries anyway
		List<String> childNodes = new ArrayList<>()
		nodes.each { JobNode node ->
			if (node.parentName.equals(parentNodeName)) {
				childNodes.add(node.name)
			}
		}
		return childNodes
	}

	/**
	 * Loads the dependency structure from a simple test file (dependencies.txt) inside the resource folder
	 * Values are separated by a comma. Every entry is in a new row. Example:
	 * b,a
	 * Reads as b depends on a.
	 */
	def loadDependencies(String content) {
		def lines = content.readLines()
		lines.each { String row ->
			def values = row.split(",")
			for(int i=1;i<values.length;i++) {
				this.addNode(new JobNode(values[0],values[i]))
			}
		}
	}

	/**
	 * Tiny helper to print all the stuff in the structure.
	 */
	def printAllDependencies() {
		nodes.each { JobNode node ->
			println(node.toString())
		}
	}
}
