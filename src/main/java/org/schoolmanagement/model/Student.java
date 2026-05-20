package org.schoolmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String surname;
    private String studentNumber;
    private String department;
    private int grade; // 1-4

    public Student() {}

    public Student(Long id, String name, String surname, String studentNumber, String department, int grade) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.studentNumber = studentNumber;
        this.department = department;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }
}
