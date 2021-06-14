/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

// @Image(cloudbees/codeship-jenkinsfile-step:maven)
// @EncryptedEnvFile(creds.encrypted)
// @ConfigFile(.ci/jenkins.yaml,jenkins.yaml)
pipeline() {
    agent {
        docker {
            image 'snoopdave/openjdk-dnd:2'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    stages {
        stage('Preparation') {
            steps {
                git(url:'https://github.com/snoopdave/rollarcus.git', branch:"jenkinsfileagent")
            }
        }
        stage('Build') {
            steps {
                dir("app") {
                    sh "mvn clean package"
                    sh "mvn com.github.spotbugs:spotbugs-maven-plugin:3.1.7:spotbugs"
                    sh "mvn pmd:pmd"
                    sh "mvn checkstyle:checkstyle"
                    archive 'target/*.jar'
                }
                configFileProvider([configFile(fileId: 'maven-test', variable: 'MAVEN_SETTINGS')]) {
                    sh 'echo $MAVEN_SETTINGS'
                }
            }
        }
        stage('Report') {
            steps {
                junit '**/target/surefire-reports/TEST-*.xml'
                //jacoco
                script {
                    def java = scanForIssues tool: [$class: 'Java']
                    def javadoc = scanForIssues tool: [$class: 'JavaDoc']
                    def checkstyle = scanForIssues tool: [$class: 'CheckStyle']
                    def pmd = scanForIssues tool: [$class: 'Pmd']
                    recordIssues enabledForFailure: true, failOnError: false, tool: spotBugs()
                    publishIssues issues: [java, javadoc, checkstyle, pmd], failOnError: false, unstableTotalAll: 29001
                }
            }
        }
//        stage('Archive') {
//            steps {
//                withAWS(credentials: "AWS") {
//                    archiveArtifacts "app/target/spotbugsXml.xml, app/target/pmd.xml, app/target/checkstyle-result.xml"
//                }
//            }
//        }
        stage('Push') {
            environment {
                registryCredential = 'dockerhubDeploy';
            }
            steps {
                script {
                    def image = docker.build('snoopdave/rollarcus');
                    docker.withRegistry('', registryCredential) {
                        image.push();
                    }
                }
            }
        }
    }
}
