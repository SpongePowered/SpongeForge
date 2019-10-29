pipeline {
    agent any

    stages {
        stage('Build') {
            environment {
                MAVEN = credentials('maven')
            }

            steps {
                withCredentials([string(credentialsId: 'spongeKeyStore', variable: 'spongeKeyStore'), string(credentialsId: 'spongeKeyStoreAlias', variable: 'spongeKeyStoreAlias'), string(credentialsId: 'spongeKeyStorePass', variable: 'spongeKeyStorePass'), string(credentialsId: 'spongeKeyStorePass', variable: 'spongeKeyStoreKeyPass')]) {

                    sh '''./gradlew --refresh-dependencies -s -PspongeUsername=$MAVEN_USR -PspongePassword=$MAVEN_PSW -PspongeKeyStore=${spongeKeyStore}  -PspongeKeyStoreAlias=${spongeKeyStoreAlias} -PspongeKeyStorePass=${spongeKeyStorePass} -PspongeKeyStoreKeyPass=${spongeKeyStoreKeyPass} -PspongeCertificateFingerprint=${spongeCertFingerprint} clean build -xsignShadowJar :uploadArchives'''
                }
            }
        }
      }
