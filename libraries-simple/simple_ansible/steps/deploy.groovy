void call(app_env) {
    String nodeName = config.nodename ?: "master"
    node(nodeName){
        stage("Deploy to ${app_env.short_name}"){
            app_env.ip_addr.each{ ip -> 
            println "Deploy to $ip "
            }
        }
    }
}
