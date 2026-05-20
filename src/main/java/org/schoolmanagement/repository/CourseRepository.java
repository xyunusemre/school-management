package org.schoolmanagement.repository;

import org.schoolmanagement.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByDepartmentIgnoreCase(String department);
}

