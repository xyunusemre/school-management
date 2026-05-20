package org.schoolmanagement.model;

public class Course {
    private Long id;
    private String code;
    private String name;
    private int credits;
    private String department;
    private Long teacherId;

    public Course() {}

    public Course(Long id, String code, String name, int credits, String department, Long teacherId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.department = department;
        this.teacherId = teacherId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}

