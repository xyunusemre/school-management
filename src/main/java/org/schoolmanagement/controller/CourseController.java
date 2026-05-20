package org.schoolmanagement.controller;

import org.schoolmanagement.model.Course;
import org.schoolmanagement.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // GET /api/courses
    @GetMapping
    public List<Course> getAll() {
        return courseService.getAll();
    }

    // GET /api/courses/1
    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        return courseService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/courses/department/Software Engineering
    @GetMapping("/department/{department}")
    public List<Course> getByDepartment(@PathVariable String department) {
        return courseService.getByDepartment(department);
    }

    // POST /api/courses
    @PostMapping
    public Course add(@RequestBody Course course) {
        return courseService.add(course);
    }

    // DELETE /api/courses/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Course> delete(@PathVariable Long id) {
        return courseService.delete(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

