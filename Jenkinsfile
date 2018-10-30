@Library('forge-shared-library')_

pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                sh './gradlew --refresh-dependencies -s clean setupDecompWorkspace'
            }
        }

        stage('Build') {
            steps {
                withCredentials([string(credentialsId: 'spongeMavenUsername', variable: 'spongeMavenUsername'), string(credentialsId: 'spongeMavenPassword', variable: 'spongeMavenPassword'), string(credentialsId: 'spongeIndexerUsername', variable: 'spongeIndexerUsername'), string(credentialsId: 'spongeIndexerPassword', variable: 'spongeIndexerPassword'), string(credentialsId: 'spongeKeyStore', variable: 'spongeKeyStore'), string(credentialsId: 'spongeKeyStoreAlias', variable: 'spongeKeyStoreAlias'), string(credentialsId: 'spongeKeyStorePass', variable: 'spongeKeyStorePass'), string(credentialsId: 'spongeKeyStorePass', variable: 'spongeKeyStoreKeyPass')]) {

                    sh '''./gradlew --refresh-dependencies -s -PforgeJenkinsPass=${forgeJenkinsPass} -PspongeKeyStore=${spongeKeyStore}  -PspongeKeyStoreAlias=${spongeKeyStoreAlias} -PspongeKeyStorePass=${spongeKeyStorePass} -PspongeKeyStoreKeyPass=${spongeKeyStoreKeyPass} -PspongeCertificateFingerprint=${spongeCertFingerprint} clean build -xsignShadowJar'''
                }
            }
            post {
                success {
                    writeChangelog(currentBuild, 'build/changelog.txt')
                        archiveArtifacts artifacts: 'build/changelog.txt', fingerprint: false
                }
            }
        }/*
        stage('publish') {
            steps {
                withCredentials([string(credentialsId: 'spongeMavenUsername', variable: 'spongeMavenUsername'), string(credentialsId: 'spongeMavenPassword', variable: 'spongeMavenPassword'), string(credentialsId: 'spongeIndexerUsername', variable: 'spongeIndexerUsername'), string(credentialsId: 'spongeIndexerPassword', variable: 'spongeIndexerPassword'), string(credentialsId: 'spongeKeyStore', variable: 'spongeKeyStore'), string(credentialsId: 'spongeKeyStoreAlias', variable: 'spongeKeyStoreAlias'), string(credentialsId: 'spongeKeyStorePass', variable: 'spongeKeyStorePass'), string(credentialsId: 'spongeKeyStorePass', variable: 'spongeKeyStoreKeyPass')]) {
sh '''#!/bin/bash -e
cat >.gradle/upload.gradle <<EOF
allprojects {
    tasks.all {
        if (it.name != \'uploadArchives\') {
            enabled = false
        }
    }
}
EOF

deploy() {
    echo "Uploading artifacts to $1"
    if ./gradlew -I .gradle/upload.gradle \\
        -s -q \\
        -PspongeRepo=$1 \\
        -PspongeUsername=$2 \\
        -PspongePassword=$3 \\
        -PforgeJenkinsPass=${spongeJenkinsPassword} \\
        :uploadArchives
    then
        echo "Successfully uploaded artifacts to $1"
    else
        echo "Failed to upload artifacts to $1"
        exit 1
    fi
}

promoteLatest() {
    echo "Promoting latest build"
    curl --user "$1:$2" http://files.minecraftforge.net/maven/manage/promote/latest/org.spongepowered.spongeforge/${BUILD_NUMBER}
}

deploy "http://files.minecraftforge.net/maven/manage/upload" "${spongeMavenUsername}" "${spongeMavenPassword}" \\
    && promoteLatest "${spongeMavenUsername}" "${spongeMavenPassword}" &
pids="$!"
deploy "https://dl-indexer.spongepowered.org/maven/upload" "${spongeIndexerUsername}" "${spongeIndexerPassword}" &
pids="$pids $!"

result=0
for pid in $pids; do
    wait $pid || result=1
done
exit $result

'''

                }
            }
        }*/
    }
    post {
        always {
            archiveArtifacts artifacts: '*/build/libs/**/*.jar', fingerprint: true
            //junit 'build/test-results/*/*.xml'
            //jacoco sourcePattern: '**/src/*/java'
        }
    }
}
