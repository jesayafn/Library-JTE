package libraries.git.steps

void checkout() {
	stage("Git Checkout"){
	
	// parsing 
	String nodeName = config.nodename ?: "master"
	node(nodeName){
		//cleanWs()
		checkout([
        	$class: 'GitSCM',
        	branches: scm.branches,
        	extensions: scm.extensions + [[$class: 'CleanCheckout']],
        	userRemoteConfigs: scm.userRemoteConfigs
    		])
		}
	}
}
