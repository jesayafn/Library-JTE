package libraries.clean.steps

void call(){
    stage("Cleaning Node Workspace"){
    
    String nodeName = config.nodename ?: "master"
    //String deletedir = config.nodename ?: "false"
    Boolean delete_dir = config.containsKey("deletedir") ? config.deletedir : false

    node(nodeName){
            println "CLEANING WORKSPACE ............"
            sh 'ls -lah'
            cleanWs deleteDirs: delete_dir
            println "CLEANING SUCCESS "
            sh 'ls -lah && pwd'
        }
    }
}



