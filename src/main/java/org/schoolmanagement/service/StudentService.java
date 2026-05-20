package org.schoolmanagement.service;

import org.schoolmanagement.model.Student;
import org.schoolmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> getById(Long id) {
        return studentRepository.findById(id);
    }

    public Student add(Student student) {
        return studentRepository.save(student);
    }

    public Optional<Student> delete(Long id) {
        Optional<Student> found = studentRepository.findById(id);
        found.ifPresent(s -> studentRepository.deleteById(id));
        return found;
    }

    public List<Student> getByDepartment(String department) {
        return studentRepository.findByDepartmentIgnoreCase(department);
    }
}
