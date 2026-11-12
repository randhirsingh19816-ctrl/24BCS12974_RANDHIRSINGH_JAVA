package com.example.studentmanagement.service;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.dto.StudentResponse;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.DuplicateStudentEmailException;
import com.example.studentmanagement.exception.InvalidStudentIdException;
import com.example.studentmanagement.exception.InvalidStudentRequestException;
import com.example.studentmanagement.exception.StudentNotFoundException;
import com.example.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        String email = normalizeEmail(request.email());
        ensureEmailAvailable(email);

        Student student = new Student(
                request.name().trim(),
                email,
                request.course().trim(),
                request.age());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentResponse::from)
                .toList();
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        return StudentResponse.from(findStudent(id));
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findStudent(id);
        String email = normalizeEmail(request.email());
        if (!email.equalsIgnoreCase(student.getEmail())) {
            ensureEmailAvailable(email);
        }

        student.setName(request.name().trim());
        student.setEmail(email);
        student.setCourse(request.course().trim());
        student.setAge(request.age());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        studentRepository.delete(findStudent(id));
    }

    @Override
    public List<StudentResponse> getStudentsByCourse(String course) {
        if (course == null || course.isBlank()) {
            throw new InvalidStudentRequestException("Course must not be blank.");
        }
        return studentRepository.findByCourseIgnoreCase(course.trim()).stream()
                .map(StudentResponse::from)
                .toList();
    }

    private Student findStudent(Long id) {
        validateId(id);
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    private void ensureEmailAvailable(String email) {
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateStudentEmailException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidStudentIdException(id);
        }
    }
}
