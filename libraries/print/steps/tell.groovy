package libraries.print.steps

void call(){
    stage("Messaging to the my world!")
    String nodeName = config.nodename ?: "master"
    node(nodeName){
        println nodeName+" on WORK!"
    }
}