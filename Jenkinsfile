pipeline {
	agent any

	options {
		ansiColor('xterm')
	}

	stages {
		stage('Build') {
			steps {
				sh 'mvn -DskipTests clean package'
			}
		}
		stage('Test') {
			steps {
				sh 'mvn test'
			}
			post {
				always {
					junit '*/target/surefire-reports/*.xml'
					publishCoverage adapters: [jacocoAdapter(mergeToOneReport: true, path: '*/target/site/jacoco/jacoco.xml')]
				}
			}
		}
		stage('SonarQube Analysis') {
			when {
				branch 'develop'
			}
			steps {
				withSonarQubeEnv('KSKE SonarQube') {
					sh 'mvn sonar:sonar'
				}
			}
		}
	}
	post {
		success {
			archiveArtifacts artifacts: '*/target/event-bus-*.jar'
		}
	}
}
