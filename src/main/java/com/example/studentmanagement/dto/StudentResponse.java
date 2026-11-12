package com.example.studentmanagement.dto;

import com.example.studentmanagement.entity.Student;

public record StudentResponse(Long id, String name, String email, String course, Integer age) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getCourse(),
                student.getAge());
    }
}
