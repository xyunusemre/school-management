package org.schoolmanagement.service;

import org.schoolmanagement.model.Course;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final List<Course> courses = new ArrayList<>();
    private long nextId = 1L;

    public CourseService() {
        courses.add(new Course(nextId++, "SWE304", "Development and Operations", 3, "Software Engineering", 1L));
        courses.add(new Course(nextId++, "SWE301", "Software Architecture",      3, "Software Engineering", 3L));
        courses.add(new Course(nextId++, "CSE201", "Data Structures",            4, "Computer Engineering", 2L));
        courses.add(new Course(nextId++, "SWE401", "Machine Learning",           3, "Software Engineering", 1L));
        courses.add(new Course(nextId++, "CSE301", "Operating Systems",          3, "Computer Engineering", 2L));
    }

    public List<Course> getAll() { return courses; }

    public Optional<Course> getById(Long id) {
        return courses.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Course add(Course course) {
        course.setId(nextId++);
        courses.add(course);
        return course;
    }

    public Optional<Course> delete(Long id) {
        Optional<Course> found = getById(id);
        found.ifPresent(courses::remove);
        return found;
    }

    public List<Course> getByDepartment(String department) {
        return courses.stream()
                .filter(c -> c.getDepartment().equalsIgnoreCase(department))
                .toList();
    }
}

