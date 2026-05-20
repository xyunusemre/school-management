package org.schoolmanagement.controller;

import org.schoolmanagement.model.Teacher;
import org.schoolmanagement.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // GET /api/teachers
    @GetMapping
    public List<Teacher> getAll() {
        return teacherService.getAll();
    }

    // GET /api/teachers/1
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getById(@PathVariable Long id) {
        return teacherService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/teachers
    @PostMapping
    public Teacher add(@RequestBody Teacher teacher) {
        return teacherService.add(teacher);
    }

    // DELETE /api/teachers/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Teacher> delete(@PathVariable Long id) {
        return teacherService.delete(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

