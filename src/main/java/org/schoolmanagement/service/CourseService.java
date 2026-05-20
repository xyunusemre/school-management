package org.schoolmanagement.service;

import org.schoolmanagement.model.Course;
import org.schoolmanagement.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public Optional<Course> getById(Long id) {
        return courseRepository.findById(id);
    }

    public Course add(Course course) {
        return courseRepository.save(course);
    }

    public Optional<Course> delete(Long id) {
        Optional<Course> found = courseRepository.findById(id);
        found.ifPresent(c -> courseRepository.deleteById(id));
        return found;
    }

    public List<Course> getByDepartment(String department) {
        return courseRepository.findByDepartmentIgnoreCase(department);
    }
}
