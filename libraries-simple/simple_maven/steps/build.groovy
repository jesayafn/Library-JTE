void call(){
    node('master'){
        stage("Maven Build"){
            println "[+] Build maven library simple"
        }
    }
}
