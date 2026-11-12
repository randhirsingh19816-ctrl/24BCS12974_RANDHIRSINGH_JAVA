package com.example.studentmanagement.exception;

public class InvalidStudentRequestException extends RuntimeException {

    public InvalidStudentRequestException(String message) {
        super(message);
    }
}
