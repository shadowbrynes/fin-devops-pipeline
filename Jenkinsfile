pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }

    stages {
        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('SonarQube Static Analysis') {
            steps {
                withSonarQubeEnv('SonarQube-Server') {
                    sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=cards-service'
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    // Halts the pipeline if SonarQube quality gate fails (bugs, security vulnerabilities, low coverage)
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build & Push to ECR') {
            steps {
                sh '''
                    aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 107203103587.dkr.ecr.us-east-1.amazonaws.com
                    docker build -t 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:${BUILD_NUMBER} .
                    docker tag 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:${BUILD_NUMBER} 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:latest
                    docker push 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:${BUILD_NUMBER}
                    docker push 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:latest
                '''
            }
        }

        stage('Deploy with Ansible') {
            steps {
                sh '''
                    cd ansible
                    ansible-playbook -i inventory_aws_ec2.yml deploy-app.yml --extra-vars "app_version=${BUILD_NUMBER}"
                '''
            }
        }
    }
}
