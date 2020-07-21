package de.testbirds;

class JobNode implements Serializable {

	String name;
	String parentName;

	/**
	 * Constructor to generate a a new node in the tree.
	 * Be aware that this is not validated and cyclic runs are possble.
	 * @param name The actual path to the prohject to trigger.
	 * @param dependsOn Which path it actually depends on.
	 */
	JobNode(name, dependsOn) {
		this.parentName = dependsOn;
		this.name = name;
	}

	/**
	 * Nice toString method.
	 * @return A string representation of the JobNode.
	 */
	def String toString() {
		return "JobNode " + this.name + " depends on " + this.parentName;
	}
}
