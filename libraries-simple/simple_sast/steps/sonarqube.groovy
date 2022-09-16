void call(){
    node('master'){
        stage("Sonarqube Scanning"){
            println "[+] Scanning source code using sonarqube simple"
        }
    }
}
