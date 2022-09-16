package libraries.sonarqube.steps

void call(Map config = [:]){
    stage("SAST SonarQube"){

        // Parsing Configuration
        String nodeName = config.nodename ?: "master"
        String sonarscannerVersion  = config.scanner_version ?: "SonarScanner 4.7"
        String serverName           = config.server_name ?: "sqce"
        Boolean withQualityGate     = config.containsKey("with_quality_gate") ? config.with_quality_gate : true
        Boolean default_project_key = config.containsKey("default_project_key") ? config.default_project_key : true

        println scm.getKey().toString()
        String gitscm = scm.getKey().toString()
        String[] git_url = gitscm.split(" ")
        String projectKey = git_url[1]

        if(default_project_key){
            projectKey = projectKey.replaceAll("http://gitlab.example.com/" , "").replaceAll("/","-").replaceAll(".git","")
        }

        node(nodeName){
            //unstash 'build-maven'
            def scannerHome = tool(sonarscannerVersion)
            withSonarQubeEnv(serverName) {
                sh "ls -lah"
                sh "JAVA_HOME=/opt/jdk-11"
                sh """${scannerHome}/bin/sonar-scanner -Dsonar.qualitygate.wait=${withQualityGate} -Dsonar.projectKey=${projectKey} -Dsonar.java.binaries=target/classes/**"""
            }
       }
    }
}
