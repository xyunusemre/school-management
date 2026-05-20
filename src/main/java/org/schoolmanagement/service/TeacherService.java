package org.schoolmanagement.service;

import org.schoolmanagement.model.Teacher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    private final List<Teacher> teachers = new ArrayList<>();
    private long nextId = 1L;

    public TeacherService() {
        teachers.add(new Teacher(nextId++, "Ahmet",  "Ozmen",  "aozmen@university.edu",  "Software Engineering", "Prof."));
        teachers.add(new Teacher(nextId++, "Zeynep", "Arslan", "zarslan@university.edu", "Computer Engineering", "Dr."));
        teachers.add(new Teacher(nextId++, "Murat",  "Korkmaz","mkorkmaz@university.edu","Software Engineering", "Assoc. Prof."));
    }

    public List<Teacher> getAll() { return teachers; }

    public Optional<Teacher> getById(Long id) {
        return teachers.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Teacher add(Teacher teacher) {
        teacher.setId(nextId++);
        teachers.add(teacher);
        return teacher;
    }

    public Optional<Teacher> delete(Long id) {
        Optional<Teacher> found = getById(id);
        found.ifPresent(teachers::remove);
        return found;
    }
}

