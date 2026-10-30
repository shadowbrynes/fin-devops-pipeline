pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }

    environment {
        AWS_SHARED_CREDENTIALS_FILE = '/var/jenkins_home/.aws/credentials'
        AWS_DEFAULT_REGION          = 'us-east-1'
        ANSIBLE_HOST_KEY_CHECKING   = 'False'
    }

    stages {
        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Terraform Provision Infrastructure') {
            steps {
                dir('terraform') {
                    sh '''
                        terraform init
                        terraform apply -auto-approve
                    '''
                }
            }
        }

        stage('SonarQube Static Analysis') {
            steps {
                withSonarQubeEnv('SonarQube-Server') {
                    dir('app') {
                        sh 'mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=cards-service'
                    }
                }
            }
        }

        stage('Quality Gate Check') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build & Push to ECR') {
            steps {
                dir('app') {
                    sh '''
                        aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 107203103587.dkr.ecr.us-east-1.amazonaws.com
                        docker build -t 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:${BUILD_NUMBER} .
                        docker tag 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:${BUILD_NUMBER} 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:latest
                        docker push 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:${BUILD_NUMBER}
                        docker push 107203103587.dkr.ecr.us-east-1.amazonaws.com/cards-service:latest
                    '''
                }
            }
        }

        stage('Deploy via Ansible Dynamic Inventory') {
            steps {
                dir('ansible') {
                    sh '''
                        ansible-playbook -i inventory_aws_ec2.yml site.yml \
                          --private-key /var/jenkins_home/.ssh/id_rsa \
                          -u ec2-user \
                          --extra-vars "app_version=${BUILD_NUMBER}"
                    '''
                }
            }
        }
    }
}
