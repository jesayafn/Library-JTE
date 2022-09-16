package libraries.openshift.steps

void call(app_env){
	stage("Deployment Openshift"){
		String nodeName = config.nodename ?: "master"
        String urlImageStream = app_env.url_imagestream ?: null
        String url_ocp = app_env.url_ocp ?: null
        String credentials = config.credentials ?: null
        String project_name = config.project_name ?: null
        String project_name_ocp = config.project_name_ocp ?: null
        String env_type = app_env.type ?: null
        String type_login = config.credentials_type  ?: {error "Please defined your type credentials user / token"}
        //String url_git = config.name_app ?: {error "Need define Application"}()
        //def name_dc = url_git.replaceAll("http://gitlab.example.com/" , "").replaceAll("/","-").replaceAll(".git","")
        //def name_dc = project_name
        //println name_dc


        println "Deployment on : ${app_env.type}"

        node(nodeName){
            def dateNow = sh(script: "date +'%d'", returnStdout: true).trim()
		    def monthNow = sh(script: "date +'%m'", returnStdout: true).trim()
		    def hourNow = sh(script: "date +'%H'", returnStdout: true)trim()
            def version = "${monthNow}.${dateNow}.${hourNow}"

            project_name = project_name.replaceAll("_" , "-").toLowerCase() 
            env_type = env_type.toLowerCase() 


            if (credentials!=null){
                println "[+] Using credentials : ${credentials}"
            }else{
                error "Please define name of credentials openshift"
            }

            if (urlImageStream!=null && url_ocp!=null && urlImageStream.length()>0 ){
                println "[+] OC LOGIN ${app_env.url_ocp}"
                oc_login(url_ocp,credentials,project_name_ocp,type_login)
            }else{
                error "Need define name of image you created......"
            }

            if (urlImageStream!=null && url_ocp!=null && urlImageStream.length()>0){
                println "[+] build image"
                build_image(project_name,version,urlImageStream,project_name_ocp , env_type)
            }else{
                error "Need define name of image you created......"
            }

            if (urlImageStream!=null && url_ocp!=null && urlImageStream.length()>0){
                println "[+] Url Image Stream : ${urlImageStream}"
                push_image(project_name,version,urlImageStream, project_name_ocp , env_type)
            }else{
                error "Need define url image stream......"
            }

            if (urlImageStream!=null && url_ocp!=null && urlImageStream.length()>0 && project_name!=null){
                println "[+] Deployment : ${project_name}"
                deploy_ocp(project_name, project_name_ocp, env_type)
            }else{
                error "Need define url image stream......"
            }

            //println checking_deployment(project_name)

        }
	}
}

void oc_login(String url, credentials, project_name_ocp, type_login){
    if(type_login=="token"){
        try {
            println "Trying to login"
            withCredentials([string(credentialsId: credentials, variable: 'TOKEN')]) {
                withEnv(["URL_OCP=${url}", "project_name_ocp=${project_name_ocp}"]) {
                    sh '''
                    set +x
                    oc login --insecure-skip-tls-verify $URL_OCP --token=$TOKEN
                    oc project $project_name_ocp
                    set -x
                '''
                } 
            }
            sh "oc whoami"
            
        } catch (any){
            println "Login Openshift failed... "
            //sh "oc login --insecure-skip-tls-verify ${url} -u ${user} -p ${token} > /dev/null"
        }
    }else{
         try {
            println "Trying to login"
            withCredentials(([usernamePassword(credentialsId: credentials, passwordVariable: 'password_ocp', usernameVariable: 'user_ocp')])) {
                withEnv(["URL_OCP=${url}", "project_name_ocp=${project_name_ocp}"]) {
                    sh '''
                    set +x
                    oc login --insecure-skip-tls-verify -u=$user_ocp -p=$password_ocp $URL_OCP
                    oc project $project_name_ocp
                    set -x
                '''
                } 
            }
            sh "oc whoami"
            
        } catch (any){
            println "Login Openshift failed... "
            //sh "oc login --insecure-skip-tls-verify ${url} -u ${user} -p ${token} > /dev/null"
        }
    }
}

void build_image(project_name, version, url_imagestream, project_name_ocp , env_type){
    url_imagestream = url_imagestream.replaceAll("https://" , "")
    
    println "[+] Building image "
    withEnv(["name_image=${project_name}" , "version=${version}", "url_registry=${url_imagestream}", "project_name=${project_name_ocp}" , "env_type=${env_type}"]){
        sh'''
        which docker
        docker build -t $name_image -f Dockerfile .
        docker tag $name_image $url_registry/$project_name/$name_image-$env_type:$version
        docker tag $name_image $url_registry/$project_name/$name_image-$env_type:latest
        '''
    }
    
}

void push_image(project_name, version, url_imagestream, project_name_ocp, env_type) {
    def url_imagestream_without_https = url_imagestream.replaceAll("https://" , "")

    println "[+] Push image to ${url_imagestream}"
    withEnv(["name_image=${project_name}" , "version=${version}", "url_registry=${url_imagestream}" , "url_imagestream_without_https=${url_imagestream_without_https}" , "project_name=${project_name_ocp}" , "env_type=${env_type}"]){
        sh'''
        set +x
        docker login -u afri -p $(oc whoami -t) $url_registry
        set -x
        docker push $url_imagestream_without_https/$project_name/$name_image-$env_type:$version
        docker push $url_imagestream_without_https/$project_name/$name_image-$env_type:latest
        '''
    }
}

void deploy_ocp(project_name, project_name_ocp, env_type){
    def check_dc = checking_deployment(project_name, env_type)
    if(check_dc){
        withEnv(["project_name=${project_name}" , "project_name_ocp=${project_name_ocp}" , "env_type=${env_type}"]){
            sh(script: 'oc new-app --image-stream=$project_name_ocp/$project_name-$env_type:latest --name $project_name-$env_type --as-deployment-config')
            sh(script: 'oc expose svc/$project_name-$env_type')
            echo "DeploymentConfig Created"
            checking = sh(script: 'oc get dc/$project_name-$env_type', returnStdout: true)
            echo checking
        }
    }else{
        println "DeploymentConfig Already Exist"
    }
}

def checking_deployment(String project_name,env_type){
    def getDc = ''
    withEnv(["project_name=${project_name}", "env_type=${env_type}"]){
        getDc = sh(script: 'oc get dc/$project_name-$env_type -o name || echo "DC Not Exists"', returnStdout: true).trim()
        if(getDc == "deploymentconfig.apps.openshift.io/${project_name}-${env_type}"){
            echo "DC Is Exist , Skip Create DC"
            echo getDc
            return false
        }else{
            println "OC CREATE APP DEPLOYMENT"
            return true
        }
    }
}
