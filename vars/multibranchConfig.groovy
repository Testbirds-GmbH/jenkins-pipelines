def load() {
	def path = "pipelines/" + JOB_NAME.replace("%2F", "/") + ".json"
	def folder = path.substring(0, path.lastIndexOf("/"))
	def fallbackPath = folder + "/multibranch.json"
	def fallbackPath2 = folder.substring(0, folder.lastIndexOf("/")) + "/multibranch.json"

	// Read most generic config first, merge others then and overwrite values
	def value = [:]
	if(fileExists(fallbackPath2)){
		value = value + readJSON(file: fallbackPath2)
	}
	if(fileExists(fallbackPath)){
		value = value + readJSON(file: fallbackPath)
	}
	if(fileExists(path)){
		value = value + readJSON(file: path)
	}
	echo "Loaded multibranch config: ${value}"
	return value
}
