# School Management App – CI/CD Pipeline

Spring Boot uygulaması → Jenkins CI/CD → Docker Hub → Minikube (Kubernetes)

## Endpoint
```
GET /hello
```

## Jenkins Pipeline Aşamaları
1. Clone – GitHub'dan kaynak kodu indir
2. Build JAR – Gradle ile jar oluştur
3. Build Docker Image – Docker image oluştur
4. DockerHub Login – Docker Hub'a giriş yap
5. Push Image – Image'ı Docker Hub'a gönder
6. Deploy to K8s – Minikube'e deploy et

## Uygulamayı Yerel Çalıştırma
```bash
./gradlew bootRun
# http://localhost:8080/hello
```

## Scale Up (2 Pod)
```bash
kubectl scale deployment school-management --replicas=2
kubectl get pods
```

