package libraries.nexus.steps

void call(){
	stage("Nexus Uploader Artifact"){
		String nodeName = config.nodename ?: "master"
		
		node(nodeName){
            pom = readMavenPom file: "pom.xml";
            filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
            echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
            artifactPath = filesByGlob[0].path;
            artifactExists = fileExists artifactPath;
            if(artifactExists) {
                nexusArtifactUploader(
                    nexusVersion: "nexus3",
                    protocol: "http",
                    nexusUrl: "10.8.60.220:8081",
                    groupId: pom.groupId,
                    version: pom.version,
                    repository: "Jenkins-Maven-Artifact",
                    credentialsId: "Nexus-User",
                    artifacts: [
                        // Artifact generated such as .jar, .ear and .war files.
                        [artifactId: pom.artifactId,
                        classifier: '',
                        file: artifactPath,
                        type: pom.packaging],

                        // Lets upload the pom.xml file for additional information for Transitive dependencies
                        [artifactId: pom.artifactId,
                        classifier: '',
                        file: "pom.xml",
                        type: "pom"]
                    ]
                );
            }
            else {
                error "*** File: ${artifactPath}, could not be found";
            }
    }
}
}
