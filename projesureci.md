# Proje Süreci — School Management CI/CD

## GENEL AKIŞ (Özet)

```
Kod (IntelliJ)
    ↓  git push
GitHub (xyunusemre/school-management)
    ↓  pollSCM (her dakika Jenkins kontrol eder)
Jenkins Pipeline (localhost:9090)
    ↓  Stage 1: Clone
    ↓  Stage 2: Gradle → JAR
    ↓  Stage 3: Docker → Image
    ↓  Stage 4: DockerHub Login
    ↓  Stage 5: DockerHub Push
    ↓  Stage 6: kubectl apply → Minikube (Kubernetes)
Minikube Cluster
    └─ Pod(s) çalışır → uygulama erişilebilir
```

---

## 1. PROJE YAPISI VE BAĞIMLILIKLARI

### 1.1 Spring Boot Uygulaması (`build.gradle`)

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'   // Spring Boot plugin
    id 'io.spring.dependency-management' version '1.1.4' // bağımlılık yönetimi
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web' // REST API için
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

**Ne işe yarar?**
- `spring-boot-starter-web` → Tomcat gömülü HTTP sunucuyu ve Spring MVC'yi içerir
- Gradle bu bağımlılıkları Maven Central'dan indirip classpath'e ekler
- `bootJar` komutu tüm bağımlılıkları tek bir "fat jar" içine paketler

### 1.2 Uygulama Kodu (`Main.java`)

```java
@SpringBootApplication   // otomatik konfigürasyon, component scan, vs.
@RestController          // bu sınıfın HTTP endpoint döndüreceğini belirtir
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args); // Spring Boot başlatır
    }

    @GetMapping("/hello")  // GET http://host/hello isteğini karşılar
    public String hello() {
        return "Hello from School Management App! Running on Kubernetes.";
    }
}
```

---

## 2. DOCKER ENTEGRASYONU

### 2.1 Dockerfile Açıklaması

```dockerfile
# ---- Stage 1: Build ----
# Gradle 8.7 + JDK17 içeren resmi image ile derleme yapılır
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . .                          # proje dosyalarını container'a kopyala
RUN gradle bootJar --no-daemon    # JAR oluştur (fat jar: bağımlılıklar dahil)

# ---- Stage 2: Run ----
# Çok daha küçük bir image: sadece JRE (JDK gerekmez, sadece çalıştırma)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Builder stage'den oluşturulan JAR'ı kopyala
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080                       # container dışına 8080 portunu aç
ENTRYPOINT ["java", "-jar", "app.jar"]  # container başlayınca çalıştırılacak komut
```

**Neden 2 aşamalı (multi-stage) build?**
- 1. aşama: Gradle ile derleme → büyük image (~1GB)
- 2. aşama: Sadece JAR + JRE → küçük image (~200MB)
- Final image'a Gradle, JDK, kaynak kod girmez → güvenli ve hızlı

### 2.2 .dockerignore

```
.gradle/    # Gradle cache (gereksiz, büyük)
build/      # önceki build çıktıları
.idea/      # IDE dosyaları
*.iml
.git/       # git geçmişi (container'a girmemeli)
```

**Neden önemli?** `COPY . .` komutu bu dosyaları atlar → daha hızlı build, daha küçük context.

### 2.3 Docker Komutları

```bash
# Image oluştur (Dockerfile'ı kullanır)
docker build -t xyunusemre/school-management:latest .

# Image'ı tag'le (opsiyonel, build sırasında da yapılabilir)
docker tag school-management:latest xyunusemre/school-management:latest

# Docker Hub'a giriş yap
docker login
# → kullanıcı adı: xyunusemre

# Image'ı Docker Hub'a push et
docker push xyunusemre/school-management:latest

# Container'ı test amaçlı çalıştır
docker run -d --rm -p 8081:8080 --name sm-test xyunusemre/school-management:latest
# -d        → arka planda çalış
# --rm      → durdurunca otomatik sil
# -p 8081:8080 → host:8081 → container:8080

# Container'ı durdur
docker stop sm-test

# Çalışan container'ları listele
docker ps

# Tüm image'ları listele
docker images

# Docker Hub'dan çıkış yap
docker logout
```

---

## 3. KUBERNETES (MİNİKUBE) ENTEGRASYONU

### 3.1 Minikube Nedir?

Minikube, yerel bilgisayarda tek node'lu bir Kubernetes cluster'ı çalıştırır.
Gerçek cloud ortamına (AWS/GCP/Azure) gerek kalmadan K8s test etmek için kullanılır.

```
[Windows Host]
    └─ Minikube (Docker driver üzerinde)
        └─ Kubernetes Node
            ├─ Pod 1: school-management container
            └─ Pod 2: school-management container (scale sonrası)
```

### 3.2 deployment.yaml Açıklaması

```yaml
apiVersion: apps/v1
kind: Deployment          # Kubernetes kaynak tipi
metadata:
  name: school-management # deployment adı
  labels:
    app: school-management
spec:
  replicas: 1             # başlangıçta 1 pod çalıştır
  selector:
    matchLabels:
      app: school-management  # bu label'a sahip pod'ları yönet
  template:               # pod şablonu
    metadata:
      labels:
        app: school-management
    spec:
      containers:
        - name: school-management
          image: xyunusemre/school-management:latest  # DockerHub'dan çekilen image
          imagePullPolicy: Always   # her deploy'da DockerHub'dan güncel image çek
          ports:
            - containerPort: 8080   # container içindeki port
```

**Deployment'ın görevi:**
- İstenilen pod sayısını (replicas) sürekli ayakta tutar
- Pod çökerse otomatik yeniden başlatır
- Rolling update ile sıfır downtime güncelleme sağlar

### 3.3 service.yaml Açıklaması

```yaml
apiVersion: v1
kind: Service                       # Kubernetes kaynak tipi
metadata:
  name: school-management-service
spec:
  type: NodePort                    # dış dünyadan erişim tipi
  selector:
    app: school-management          # hangi pod'lara yönlendir
  ports:
    - protocol: TCP
      port: 80          # service portu (cluster içi)
      targetPort: 8080  # pod'daki uygulama portu
      nodePort: 30080   # host makinede açılan port (30000-32767 arası)
```

**Service'in görevi:**
- Pod'lar IP adresi değişse bile sabit bir erişim noktası sağlar
- NodePort: host_ip:30080 → pod:8080 yönlendirmesi yapar
- Birden fazla pod varsa aralarında load balancing yapar

### 3.4 Kubernetes Komutları

```bash
# Minikube başlat
minikube start

# Minikube durumunu kontrol et
minikube status

# Deployment oluştur / güncelle
kubectl apply -f k8s/deployment.yaml --validate=false

# Service oluştur / güncelle
kubectl apply -f k8s/service.yaml --validate=false

# Tüm pod'ları listele (durum, yaş, restart sayısı)
kubectl get pods

# Tüm service'leri listele (tip, IP, port)
kubectl get services

# Deployment bilgilerini göster
kubectl get deployments

# Deployment'ı yeniden başlat (yeni image'ı çeker)
kubectl rollout restart deployment/school-management

# Deployment'ın tamamlanmasını bekle (CI/CD için kritik)
kubectl rollout status deployment/school-management --timeout=120s

# POD loglarını izle
kubectl logs -f <pod-adı>
# Örnek: kubectl logs -f school-management-cb7dd484d-vj7zb

# Pod'a terminal bağlan
kubectl exec -it <pod-adı> -- /bin/sh

# Service'i yerel porta yönlendir (Windows'ta minikube tunnel çalışmadığında)
kubectl port-forward service/school-management-service 7779:80

# Uygulamayı 2 pod'a scale et (sunum için)
kubectl scale deployment school-management --replicas=2

# 1 pod'a geri döndür
kubectl scale deployment school-management --replicas=1

# Deployment sil
kubectl delete deployment school-management

# Service sil
kubectl delete service school-management-service

# Tüm kaynakları tek seferde sil
kubectl delete -f k8s/

# Cluster olaylarını izle
kubectl get events --sort-by='.lastTimestamp'

# Node bilgisi
kubectl get nodes

# Ayrıntılı pod bilgisi (hata ayıklama için)
kubectl describe pod <pod-adı>
```

---

## 4. JENKINS CI/CD PIPELINE

### 4.1 Jenkins Nedir?

Jenkins, kod değişikliklerini otomatik olarak algılayıp; derleme, test, paketleme ve
deploy işlemlerini sırasıyla çalıştıran bir otomasyon sunucusudur.

**Bu projede:** GitHub'a her push yapıldığında (pollSCM ile algılanır) tüm pipeline
otomatik tetiklenir.

### 4.2 Jenkinsfile Açıklaması

```groovy
pipeline {
    agent any   // herhangi bir Jenkins agent üzerinde çalış

    environment {
        // dockerhub-credentials ID'li credentials'tan kullanıcı adı/şifre al
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        IMAGE_NAME = "${DOCKERHUB_CREDENTIALS_USR}/school-management"
        IMAGE_TAG  = "latest"
        // Jenkins farklı kullanıcı altında çalışır, kubeconfig'i tam yol ver
        KUBECONFIG = "C:\\Users\\khrmn\\.kube\\config"
    }

    triggers {
        pollSCM('* * * * *')  // her dakika GitHub'ı kontrol et, değişiklik varsa çalıştır
    }

    stages {
        stage('Stage 1: Clone Project') {
            steps {
                checkout scm  // Jenkinsfile'ın geldiği repo'yu workspace'e kopyala
            }
        }

        stage('Stage 2: Build JAR') {
            steps {
                // Windows'ta .bat komutu ile Gradle wrapper çalıştır
                bat 'gradlew.bat bootJar --no-daemon'
                // Çıktı: build/libs/school-management-1.0-SNAPSHOT.jar
            }
        }

        stage('Stage 3: Build Docker Image') {
            steps {
                // Dockerfile'ı kullanarak image oluştur
                bat "docker build -t %IMAGE_NAME%:%IMAGE_TAG% ."
            }
        }

        stage('Stage 4: Login to DockerHub') {
            steps {
                // Şifre stdin'den okunur (güvenli, log'a yazdırılmaz)
                bat "echo %DOCKERHUB_CREDENTIALS_PSW% | docker login -u %DOCKERHUB_CREDENTIALS_USR% --password-stdin"
            }
        }

        stage('Stage 5: Push Image to DockerHub') {
            steps {
                bat "docker push %IMAGE_NAME%:%IMAGE_TAG%"
                // DockerHub: hub.docker.com/r/xyunusemre/school-management
            }
        }

        stage('Stage 6: Deploy to Kubernetes') {
            steps {
                // --validate=false: K8s API schema validation'ı atla (bağlantı sorunu için)
                bat 'kubectl apply -f k8s/deployment.yaml --validate=false'
                bat 'kubectl apply -f k8s/service.yaml --validate=false'
                // Yeni image'ı çekmesi için restart
                bat 'kubectl rollout restart deployment/school-management'
                // Deploy tamamlanana kadar bekle (başarısızsa pipeline hata verir)
                bat 'kubectl rollout status deployment/school-management --timeout=120s'
            }
        }
    }

    post {
        success { echo 'Pipeline completed successfully!' }
        failure { echo 'Pipeline failed. Check the logs above.' }
        always  { bat 'docker logout' }  // her durumda çıkış yap
    }
}
```

### 4.3 Jenkins Credential Ayarı

```
Manage Jenkins
  → Credentials
    → System
      → Global credentials
        → Add Credentials
          Kind:     Username with password
          Username: xyunusemre          (Docker Hub kullanıcı adı)
          Password: ****                (Docker Hub şifresi)
          ID:       dockerhub-credentials   ← Jenkinsfile'daki ID ile aynı olmalı!
```

### 4.4 Jenkins Pipeline Job Ayarı

```
New Item → school-management-pipeline → Pipeline
  Pipeline sekmesi:
    Definition:      Pipeline script from SCM
    SCM:             Git
    Repository URL:  https://github.com/xyunusemre/school-management.git
    Branch:          */main
    Script Path:     Jenkinsfile
```

---

## 5. GITHUB ENTEGRASYONU

### 5.1 Git Komutları

```bash
# Yeni repo başlat
git init

# Tüm dosyaları stage'e ekle
git add .

# İlk commit
git commit -m "Initial commit: Spring Boot CI/CD with Jenkins and Kubernetes"

# Remote repo bağla
git remote add origin https://github.com/xyunusemre/school-management.git

# Ana branch adını main yap
git branch -M main

# GitHub'a push et
git push -u origin main

# Değişiklik sonrası push
git add .
git commit -m "açıklama"
git push
```

### 5.2 pollSCM Nasıl Çalışır?

```
Jenkins (her dakika):
  → GitHub API'ye sor: "Son commit hash değişti mi?"
  → Evet (push var) → Pipeline'ı tetikle
  → Hayır           → Beklemeye devam et
```

---

## 6. TÜM SİSTEMİN BİRLEŞİK AKIŞI

```
[1] Developer: kod yazar, git push yapar
       ↓
[2] GitHub: kodu depolar (xyunusemre/school-management)
       ↓
[3] Jenkins (pollSCM): değişikliği fark eder, pipeline başlatır
       ↓
[4] Stage 1 - Clone:
    Jenkins → GitHub'dan kodu çeker
    Konum: C:\ProgramData\Jenkins\.jenkins\workspace\school-management-pipeline\
       ↓
[5] Stage 2 - Build JAR:
    gradlew.bat bootJar
    Sonuç: build/libs/school-management-1.0-SNAPSHOT.jar
    (Spring Boot + tüm bağımlılıklar tek JAR içinde)
       ↓
[6] Stage 3 - Docker Image:
    docker build -t xyunusemre/school-management:latest .
    Dockerfile çalışır:
      1. Gradle container'da JAR oluşturur
      2. Küçük Alpine+JRE image'ına JAR kopyalanır
    Sonuç: ~200MB Docker image
       ↓
[7] Stage 4 - DockerHub Login:
    Credentials store'dan şifre alınır, güvenli login yapılır
       ↓
[8] Stage 5 - Push:
    docker push xyunusemre/school-management:latest
    Image DockerHub'a yüklenir
       ↓
[9] Stage 6 - Kubernetes Deploy:
    kubectl apply -f k8s/deployment.yaml
    → Minikube, DockerHub'dan yeni image'ı çeker (imagePullPolicy: Always)
    → Eski pod durdurulur, yeni pod başlatılır (rolling update)
    kubectl apply -f k8s/service.yaml
    → NodePort service, pod'a trafik yönlendirir
       ↓
[10] Uygulama çalışır:
     kubectl port-forward service/school-management-service 7779:80
     → http://localhost:7779/hello
     → "Hello from School Management App! Running on Kubernetes."
```

---

## 7. SUNUM İÇİN ÖNEMLİ KOMUTLAR

```bash
# ---- Kubernetes durumu göster ----
kubectl get pods                    # pod listesi ve durumları
kubectl get services                # service listesi ve portları
kubectl get deployments             # deployment listesi

# ---- Uygulamayı test et ----
kubectl port-forward service/school-management-service 7779:80
# Tarayıcıda: http://localhost:7779/hello

# ---- 2 pod'a scale et ----
kubectl scale deployment school-management --replicas=2
kubectl get pods                    # 2 pod göster

# ---- Scale sonrası test ----
# Hâlâ çalışıyor mu?
Invoke-RestMethod -Uri "http://localhost:7779/hello" -UseBasicParsing

# ---- Jenkins pipeline logları ----
# http://localhost:9090 → school-management-pipeline → #son_build → Console Output

# ---- DockerHub'da image kontrol ----
# https://hub.docker.com/r/xyunusemre/school-management

# ---- Minikube dashboard (görsel) ----
minikube dashboard
```

---

## 8. HATA ÇÖZME NOTLARI

### Sorun 1: `kubectl apply` → `context deadline exceeded`
**Sebep:** Jenkins farklı kullanıcı altında çalışır, kubeconfig'i bulamaz.
**Çözüm:** Jenkinsfile'da `KUBECONFIG = "C:\\Users\\khrmn\\.kube\\config"` tanımlandı.

### Sorun 2: `port already in use`
**Sebep:** İstenen port başka uygulama tarafından kullanılıyor.
**Çözüm:** Farklı port dene: `kubectl port-forward ... 7779:80`

### Sorun 3: Windows'ta `minikube service --url` takılıyor
**Sebep:** Docker driver'da Windows tüneli arka planda çalışır, terminal bloklanır.
**Çözüm:** `kubectl port-forward` kullan.

---

## 9. TEKNOLOJİ STACKİ ÖZETİ

| Teknoloji | Versiyon | Görev |
|-----------|----------|-------|
| Java | 17 | Uygulama dili |
| Spring Boot | 3.2.5 | Web framework, gömülü Tomcat |
| Gradle | 8.12 | Build aracı, bağımlılık yönetimi |
| Docker | Desktop | Image build, container çalıştırma |
| Docker Hub | - | Image deposu (registry) |
| Minikube | Latest | Yerel Kubernetes cluster |
| kubectl | Latest | Kubernetes CLI |
| Jenkins | Latest | CI/CD otomasyon sunucusu |
| GitHub | - | Kaynak kod deposu |

