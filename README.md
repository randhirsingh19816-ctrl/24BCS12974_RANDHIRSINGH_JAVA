# Student Management System

A Spring Boot REST API for creating, reading, updating, and deleting student records stored in PostgreSQL.

## Features

- CRUD operations for student records.
- Case-insensitive student lookup by course.
- Request validation for names, email addresses, course names, and ages.
- Case-insensitive unique email handling.
- Centralized JSON errors for invalid input, duplicate emails, missing students, and database failures.
- Postman collection with automated response assertions.

## Technology

- Java 17
- Spring Boot 3.5.6
- Spring Web, Spring Data JPA, and Jakarta Validation
- PostgreSQL and Hibernate
- Maven
- JUnit 5, Spring MVC Test, and Mockito

## Architecture

The application uses a layered design:

- `entity`: JPA persistence model.
- `repository`: Spring Data repository and derived queries.
- `service`: business rules and transaction boundaries.
- `controller`: HTTP routes and request/response mapping.
- `dto`: validated request and response types.
- `exception`: domain exceptions and centralized REST error responses.

## Project structure

```text
src/
  main/
    java/com/example/studentmanagement/
      StudentManagementApplication.java
      controller/StudentController.java
      dto/
      entity/Student.java
      exception/
      repository/StudentRepository.java
      service/
    resources/application.properties
  test/
    java/com/example/studentmanagement/controller/StudentControllerTest.java
postman/Student Management System.postman_collection.json
```

## PostgreSQL setup

Install and start PostgreSQL, then create the database:

```sql
CREATE DATABASE student_management;
```

The application connects to `localhost:5432` and uses the `postgres` username by default. Configure the password outside the repository. In PowerShell, for the current terminal:

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-postgres-password"
```

`DB_USERNAME` and `DB_PASSWORD` may also be configured in your IDE's run configuration or operating-system environment. The application does not contain a database password. Hibernate creates or updates the `students` table using `spring.jpa.hibernate.ddl-auto=update`.

## Run and test

From the `demo` project directory:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The HTTP tests use a mocked service and do not require a running database. Running the application does require PostgreSQL to be running with the database and credentials configured above.

## API

Base URL: `http://localhost:8080/api/students`

| Method | Path | Success |
|---|---|---|
| `POST` | `/api/students` | `201 Created` |
| `GET` | `/api/students` | `200 OK` |
| `GET` | `/api/students/{id}` | `200 OK` |
| `PUT` | `/api/students/{id}` | `200 OK` |
| `DELETE` | `/api/students/{id}` | `204 No Content` |
| `GET` | `/api/students/course/{course}` | `200 OK` |

Course matching is case-insensitive. Student IDs must be positive. An email is normalized to lowercase before storage so uniqueness checks are case-insensitive.

### Create student

```http
POST http://localhost:8080/api/students
Content-Type: application/json
```

```json
{
  "name": "Rahul",
  "email": "rahul@gmail.com",
  "course": "CSE",
  "age": 21
}
```

Example response (`201 Created`):

```json
{
  "id": 1,
  "name": "Rahul",
  "email": "rahul@gmail.com",
  "course": "CSE",
  "age": 21
}
```

### Update student

```http
PUT http://localhost:8080/api/students/1
Content-Type: application/json
```

```json
{
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "course": "AIML",
  "age": 22
}
```

### Read and delete

```http
GET    http://localhost:8080/api/students
GET    http://localhost:8080/api/students/1
GET    http://localhost:8080/api/students/course/CSE
DELETE http://localhost:8080/api/students/1
```

### Errors

Errors use a JSON body with a message and HTTP status:

```json
{
  "message": "Student not found with ID: 10",
  "status": 404
}
```

Invalid input returns `400`, duplicate email returns `409`, missing students return `404`, and database access errors return `503`.

## Postman

Import [`postman/Student Management System.postman_collection.json`](postman/Student%20Management%20System.postman_collection.json) into Postman. Its `baseUrl` collection variable defaults to `http://localhost:8080/api/students`. Run the collection in order: the create request stores its returned ID as `studentId`, which is used by subsequent requests. Each request includes a test script for its status and relevant response shape.

The collection includes create, list, get by ID, update, delete, get by course, nonexistent ID, duplicate email, and invalid input requests. Ensure PostgreSQL and the application are running before sending requests.
