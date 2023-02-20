// package libraries.print_text.steps

void call(){
    stage("Messaging to the my world!")
    String nodeName = config.nodename ?: "master"
    node(nodeName){
        println nodeName+" on WORK!"
    }
}