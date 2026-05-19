pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        DOCKERHUB_USERNAME    = "${DOCKERHUB_CREDENTIALS_USR}"
        IMAGE_NAME            = "${DOCKERHUB_CREDENTIALS_USR}/school-management"
        IMAGE_TAG             = "latest"
        KUBECONFIG            = "C:\\Users\\khrmn\\.kube\\config"
    }

    triggers {
        // GitHub push webhook (ngrok ile dışa açıkken aktif olur)
        // githubPush()
        // Alternatif: her dakika GitHub'ı kontrol et
        pollSCM('* * * * *')
    }

    stages {

        stage('Stage 1: Clone Project') {
            steps {
                echo 'Cloning project from GitHub...'
                checkout scm
            }
        }

        stage('Stage 2: Build JAR') {
            steps {
                echo 'Building project with Gradle...'
                bat 'gradlew.bat bootJar --no-daemon'
            }
        }

        stage('Stage 3: Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                bat "docker build -t %IMAGE_NAME%:%IMAGE_TAG% ."
            }
        }

        stage('Stage 4: Login to DockerHub') {
            steps {
                echo 'Logging in to DockerHub...'
                bat "echo %DOCKERHUB_CREDENTIALS_PSW% | docker login -u %DOCKERHUB_CREDENTIALS_USR% --password-stdin"
            }
        }

        stage('Stage 5: Push Image to DockerHub') {
            steps {
                echo 'Pushing image to DockerHub...'
                bat "docker push %IMAGE_NAME%:%IMAGE_TAG%"
            }
        }

        stage('Stage 6: Deploy to Kubernetes') {
            steps {
                echo 'Applying Kubernetes manifests...'
                bat 'kubectl apply -f k8s/deployment.yaml --validate=false'
                bat 'kubectl apply -f k8s/service.yaml --validate=false'
                bat 'kubectl rollout restart deployment/school-management'
                bat 'kubectl rollout status deployment/school-management --timeout=120s'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully! Application deployed to Minikube.'
        }
        failure {
            echo 'Pipeline failed. Check the logs above.'
        }
        always {
            bat 'docker logout'
        }
    }
}

