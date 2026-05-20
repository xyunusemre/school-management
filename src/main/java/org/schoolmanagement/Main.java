package org.schoolmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
@RestController
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // Ana sayfa
    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "app",     "School Management System",
            "version", "1.0",
            "status",  "Running on Kubernetes",
            "endpoints", "/api/students | /api/teachers | /api/courses"
        );
    }

    // Eski endpoint, geriye dönük uyumluluk
    @GetMapping("/hello")
    public String hello() {
        return "Hello from School Management System v2.0! Running on Kubernetes.";
    }

    // Health check
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "school-management");
    }
}