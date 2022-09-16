package libraries.test.steps

void hi(Map config = [:]) {
    stage("Hello"){
    
    // parsing
    String nodeName = config.nodename ?: "master"

    node(nodeName){
        sh "echo Hello ${config.name}. Day is ${config.dayOfWeek}"
        sh "ls -lah target/"
        }
    }
}
