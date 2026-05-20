package org.schoolmanagement.service;

import org.schoolmanagement.model.Teacher;
import org.schoolmanagement.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    public Optional<Teacher> getById(Long id) {
        return teacherRepository.findById(id);
    }

    public Teacher add(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public Optional<Teacher> delete(Long id) {
        Optional<Teacher> found = teacherRepository.findById(id);
        found.ifPresent(t -> teacherRepository.deleteById(id));
        return found;
    }
}
