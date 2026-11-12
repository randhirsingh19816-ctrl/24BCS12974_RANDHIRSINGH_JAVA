package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.dto.StudentResponse;
import com.example.studentmanagement.exception.DuplicateStudentEmailException;
import com.example.studentmanagement.exception.InvalidStudentIdException;
import com.example.studentmanagement.exception.StudentNotFoundException;
import com.example.studentmanagement.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @Test
    void createStudentReturnsCreatedStudentAndLocation() throws Exception {
        when(studentService.createStudent(any(StudentRequest.class)))
                .thenReturn(student(1L, "Rahul", "rahul@gmail.com", "CSE", 21));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Rahul","email":"rahul@gmail.com","course":"CSE","age":21}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/students/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("rahul@gmail.com"));
    }

    @Test
    void getAllStudentsReturnsList() throws Exception {
        when(studentService.getAllStudents())
                .thenReturn(List.of(student(1L, "Rahul", "rahul@gmail.com", "CSE", 21)));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rahul"));
    }

    @Test
    void getStudentByIdReturnsStudent() throws Exception {
        when(studentService.getStudentById(1L))
                .thenReturn(student(1L, "Rahul", "rahul@gmail.com", "CSE", 21));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateStudentReturnsUpdatedStudent() throws Exception {
        when(studentService.updateStudent(eq(1L), any(StudentRequest.class)))
                .thenReturn(student(1L, "Rahul Sharma", "rahul@gmail.com", "AIML", 22));

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Rahul Sharma","email":"rahul@gmail.com","course":"AIML","age":22}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rahul Sharma"))
                .andExpect(jsonPath("$.course").value("AIML"));
    }

    @Test
    void deleteStudentReturnsNoContent() throws Exception {
        doNothing().when(studentService).deleteStudent(1L);

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService).deleteStudent(1L);
    }

    @Test
    void getStudentsByCourseReturnsMatchingStudents() throws Exception {
        when(studentService.getStudentsByCourse("CSE"))
                .thenReturn(List.of(student(1L, "Rahul", "rahul@gmail.com", "CSE", 21)));

        mockMvc.perform(get("/api/students/course/CSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].course").value("CSE"));
    }

    @Test
    void missingStudentReturnsNotFoundError() throws Exception {
        when(studentService.getStudentById(99L)).thenThrow(new StudentNotFoundException(99L));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found with ID: 99"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void nonPositiveIdReturnsBadRequest() throws Exception {
        when(studentService.getStudentById(0L)).thenThrow(new InvalidStudentIdException(0L));

        mockMvc.perform(get("/api/students/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void nonNumericIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/students/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'."))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        when(studentService.createStudent(any(StudentRequest.class)))
                .thenThrow(new DuplicateStudentEmailException());

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Rahul","email":"rahul@gmail.com","course":"CSE","age":21}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void invalidRequestBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email","course":"CSE","age":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.status").value(400));
    }

    private StudentResponse student(Long id, String name, String email, String course, Integer age) {
        return new StudentResponse(id, name, email, course, age);
    }
}
