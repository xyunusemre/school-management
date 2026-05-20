package org.schoolmanagement.service;

import org.schoolmanagement.model.Student;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final List<Student> students = new ArrayList<>();
    private long nextId = 1L;

    public StudentService() {
        // Örnek veriler
        students.add(new Student(nextId++, "Ahmet",   "Yilmaz", "20210001", "Software Engineering", 3));
        students.add(new Student(nextId++, "Ayse",    "Kaya",   "20210002", "Software Engineering", 3));
        students.add(new Student(nextId++, "Mehmet",  "Demir",  "20220001", "Computer Engineering", 2));
        students.add(new Student(nextId++, "Fatma",   "Celik",  "20230001", "Software Engineering", 1));
        students.add(new Student(nextId++, "Ali",     "Sahin",  "20200001", "Computer Engineering", 4));
    }

    public List<Student> getAll() {
        return students;
    }

    public Optional<Student> getById(Long id) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Student add(Student student) {
        student.setId(nextId++);
        students.add(student);
        return student;
    }

    public Optional<Student> delete(Long id) {
        Optional<Student> found = getById(id);
        found.ifPresent(students::remove);
        return found;
    }

    public List<Student> getByDepartment(String department) {
        return students.stream()
                .filter(s -> s.getDepartment().equalsIgnoreCase(department))
                .toList();
    }
}

