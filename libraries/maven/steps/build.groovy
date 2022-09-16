package libraries.maven.steps

void call(){
	stage("Build Maven"){
		String nodeName = config.nodename ?: "master"
		
		node(nodeName){
			withEnv(['JAVA_HOME=/opt/jdk-11']){
				sh "java -version"
				sh "mvn -v"
				sh "mvn clean package && ls -lah"
			}
	}
	}
}
