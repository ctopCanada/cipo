@Library('ised-cicd-lib') _

pipeline {
	agent {
       	label 'maven'
   	}
   	
    options {
        disableConcurrentBuilds()
    }
  
   	environment {
		// GLobal Vars
		IMAGE_NAME = "corpcan-sts-integration"
    }
  
    stages {
    	stage('build') {
			steps {
				script{
	    			builder.buildMaven("${IMAGE_NAME}")
				}
			}
    	}
    }
}
