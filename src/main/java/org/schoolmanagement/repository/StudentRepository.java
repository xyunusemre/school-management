package org.schoolmanagement.repository;

import org.schoolmanagement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByDepartmentIgnoreCase(String department);
}

