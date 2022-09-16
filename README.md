# Jenkins JTE 

## Prerequisites

- Internet Connection
- Jenkins VM
- Gitlab VM
- Nexus VM


## Plugin Jenkins 

- template engine
- pipeline-utility
- sonarqube
- docker
- gitlab
- gitlab branch sources
- multibranch-pipeline
- multibranch scan webhook trigger
- Nexus Artifact Uploader

## Template Engine 
Most pipelines are going to follow the same generic workflow regardless of which specific tech stack is employed by an application. The Templating Engine Plugin (abbreviated as JTE for Jenkins Templating Engine) allows you to capture this efficiency by creating tool-agnostic, templated workflows to be reused by every team.

## Struct JTE

- Pipeline Template
```groovy
message()
deploy(dev)
deploy(prod)
```

- Libraries
```groovy
void call() {

    String nodeName = config.nodename ?: "master"
    node(nodeName){
        stage("Message node ${nodeName}"){
           
        }
    }
}
```

- Pipeline Configuration
```groovy
libraries{
    simple_mesagge {nodename: "master"}
}
stages{
    ci{
        build
        unit_test
    }
    cd{ 
        upload_artifact
    }
}
application_environments{
    dev
    prod
}
```

## Basic Pipeline Jenkins

### Declarative Pipeline
```groovy
pipeline {
    agent any
    stages {
        stage('Build'){
            steps{
                //
            }
        }
        stage('Test'){
            steps{
                //
            }
        }
        stage('Deploy'){
            steps{
                //
            }
        }
    }
}
```
### Scripted Pipeline
```groovy
node('master'){
    stage('Build'){
        //
    }
}
node('master'){
    stage('Test'){
        //
    }
}
node('master'){
    stage('Deploy'){
        //
    }
}
```
```groovy
node('master'){
    stage('Build'){
        //
    }
    stage('Test'){
        //
    }
    stage('Deploy'){
        //
    }
}
```


## Check pipeline Jenkins JTE

### Create testing pipeline job
First going to Dashboard Jenkins > New Item > Pipeline > Jenkins Template Engine

```groovy
docker.image("maven").inside{
    sh "mvn --version"
}
```
![](2022-09-04-22-28-27.png)

Then save and build 


## Prepare JTE

### Creating PAT Gitlab For Jenkins

Login Gitlab > Profile > Access Token 
![](2022-09-04-23-37-25.png)

Login Jenkins > Manage Jenkins > Manage Credentials > Jenkins > Global Credentials > Add Credentials > Gitlab Personal Access Token 

![](2022-09-04-23-40-31.png)

## Create Simple library

### Create library
Create directory structure like this :

```bash
.
├── simple_ansible
│   └── steps
│       └── deploy.groovy
├── simple_deploy
│   └── steps
│       └── deploy.groovy
├── simple_maven
│   └── steps
│       └── build.groovy
└── simple_sast
    └── steps
        └── sonarqube.groovy
```
- simple_maven/steps/build.groovy :
```groovy
void call(){
    node('master'){
        stage("Maven Build"){
            println "[+] Build maven library simple"
        }
    }
}
```

- simple_sast/steps/sonarqube.groovy :
```groovy
void call(){
    node('master'){
        stage("Sonarqube Scanning"){
            println "[+] Scanning source code using sonarqube simple"
        }
    }
}
```

- simple_deploy/steps/deploy.groovy :
```groovy
void call(){
    node('master'){
        stage("Deploy"){
            println "[+] Deployment simple"
        }
    }
}
```

- simple_maven/steps/unit_test.groovy :
```groovy
void call(){
    node('master'){
        stage("Maven Unit Test"){
            println "[+] Unit-test maven library simple"
        }
    }
}
```

- simple_nexus/steps/upload_artifact.groovy :
```groovy
void call(){
    node('master'){
        stage("Upload Artifact Maven"){
            println "[+] Uploading artifact maven to nexus "
        }
    }
}
```

- Then push to gitlab

### Configure Source library jenkins

Dashboard Jenkins > Manage Jenkins > Configure System > Jenkins Templating Engine > Add library sources 

![](2022-09-05-10-11-07.png)

### Create Basic pipeline JTE

Dashboard jenkins > New Item > Pipeline 
- Select pipeline : Jenkins Template Engine 
- Select pipeline configuration : From Console

// gambar 

pipeline template :

```groovy
build()
unit_test()
sonarqube()
upload_artifact()
deploy()
```

pipeline configuration :

```groovy
libraries{
    simple_maven
    simple_sast
    simple_nexus
    simple_deploy
}
```

## JTE Primitives

### Stages
> Note : Stages are a mechanism for chaining multiple steps together to be invoked through an aliased method name.

pipeline template :

```groovy
ci()
cd()
deploy()
```

pipeline configuration :

```groovy
stages{
    ci{
        build
        unit_test
    }
    cd{ 
        upload_artifact
    }
}
libraries{
    simple_maven
    simple_sast
    simple_deploy
    simple_nexus
}
```


### Application Environment

Library :
```
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
```

pipeline template :

```groovy
deploy()
```

pipeline configuration :

```groovy

application_environments{
    staging{
        short_name = "Staging"
        ip_addr = ["10.1.1.10" , "10.1.1.10"]
    }
    prod{
        short_name = "production"
        ip_addr = ["10.1.1.10" , "10.1.1.10"]
    }

}

libraries{
    simple_ansible {nodename: "master"}
}
```

## Advance 
Coming Soon