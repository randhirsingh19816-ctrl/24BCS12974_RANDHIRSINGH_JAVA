package com.example.studentmanagement.exception;

public class InvalidStudentIdException extends RuntimeException {

    public InvalidStudentIdException(Long id) {
        super("Student ID must be a positive number: " + id);
    }
}
