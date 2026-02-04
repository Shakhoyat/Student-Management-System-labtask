# Student Management System

A Spring Boot web application for managing students, teachers, courses, and departments with **industry-standard Spring Security authentication and authorization**.

## Features

### Roles
- **Teacher** - Full access to create, edit, and delete all entities
- **Student** - Can view all entities and edit their own profile (except role)

### Entities & Relationships

| Entity | Relationships |
|--------|---------------|
| **Student** | ManyToMany with Teachers, ManyToMany with Courses |
| **Teacher** | ManyToMany with Students, ManyToOne with Department |
| **Course** | ManyToMany with Students, ManyToOne with Department |
| **Department** | OneToMany with Teachers, OneToMany with Courses |

### Authorization Rules
- Teachers can create, edit, and delete student profiles
- Students can only edit their own profile
- Students cannot change their role

## Tech Stack

- **Backend:** Spring Boot 4.0.1, Spring Data JPA, Hibernate
- **Security:** Spring Security 7, BCrypt password hashing
- **Frontend:** Thymeleaf, HTML, CSS, Thymeleaf Spring Security Extras
- **Database:** PostgreSQL 16
- **Containerization:** Docker, Docker Compose

## 🔐 Spring Security Implementation

### Authentication

The application uses **Spring Security** with the following features:

| Feature | Implementation |
|---------|----------------|
| **Password Encoding** | BCrypt (10 rounds) |
| **User Storage** | PostgreSQL `users` table |
| **Session Management** | Server-side sessions with JSESSIONID cookie |
| **Login** | Form-based authentication |
| **CSRF Protection** | Enabled by default |

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // BCrypt password encoder
    // DaoAuthenticationProvider with UserDetailsService
    // Form login with custom login page
    // Role-based URL authorization
}
```

### Authorization (Method-Level Security)

The application uses `@PreAuthorize` annotations for fine-grained access control:

```java
// Teacher-only operations
@PreAuthorize("hasRole('TEACHER')")
public String createStudent(...) { }

// Student can edit own profile, Teacher can edit all
@PreAuthorize("hasRole('TEACHER') or (hasRole('STUDENT') and @securityService.isOwnProfile(#id, authentication))")
public String editStudent(@PathVariable Long id, ...) { }
```

### URL Security Rules

| URL Pattern | Access |
|-------------|--------|
| `/`, `/auth/login`, `/auth/register`, `/css/**` | Public |
| `/students/new`, `/students/*/delete` | TEACHER only |
| `/teachers/**` (create/edit/delete) | TEACHER only |
| `/courses/**` (create/edit/delete) | TEACHER only |
| `/departments/**` (create/edit/delete) | TEACHER only |
| All other URLs | Authenticated users |

### User Entity (for Authentication)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,  -- BCrypt hash
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL,       -- STUDENT or TEACHER
    enabled BOOLEAN DEFAULT true,
    profile_id BIGINT                -- Links to student/teacher profile
);
```

## Getting Started

### Prerequisites
- Docker and Docker Compose installed
- Java 17+ (for local development)
- Maven (or use included wrapper)

### Run with Docker Compose

```bash
# Build and start the application
docker-compose up --build -d

# View logs
docker-compose logs -f app

# Stop the application
docker-compose down
```

The application will be available at: **http://localhost:8080**

### Run Locally (Development)

1. Start PostgreSQL:
```bash
docker-compose up -d postgres
```

2. Run the Spring Boot application:
```bash
./mvnw spring-boot:run
```

## Usage

1. Open http://localhost:8080
2. Register a new account or login with existing credentials:
   - **Teacher account:** `teacher` / `password`
   - **Student account:** `student` / `password`

### As a Teacher, you can:
- Create, edit, and delete **Students**
- Create, edit, and delete **Teachers**
- Create, edit, and delete **Courses**
- Create, edit, and delete **Departments**
- Assign students to courses
- Assign teachers to students

### As a Student, you can:
- View all entities
- Edit your own profile (name, roll, email)
- Cannot change your role

## Project Structure

```
src/main/java/com/example/webapp/
├── WebappApplication.java      # Main application
├── config/                     # Configuration classes
├── controller/                 # MVC Controllers
│   ├── AuthController.java
│   ├── StudentController.java
│   ├── TeacherController.java
│   ├── CourseController.java
│   └── DepartmentController.java
├── dto/                        # Data Transfer Objects
├── entity/                     # JPA Entities
│   ├── Student.java
│   ├── Teacher.java
│   ├── Course.java
│   ├── Department.java
│   └── Role.java
├── repository/                 # Spring Data Repositories
└── service/                    # Business Logic Services

src/main/resources/
├── application.yml             # Application configuration
├── static/css/                 # Stylesheets
└── templates/                  # Thymeleaf templates
```

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│   departments   │         │    teachers     │         │    students     │
├─────────────────┤         ├─────────────────┤         ├─────────────────┤
│ PK id           │◄────────│ FK department_id│         │ PK id           │
│    name (UNIQUE)│         │ PK id           │◄───┐    │    name         │
└─────────────────┘         │    name         │    │    │    roll (UNIQUE)│
        │                   │    email        │    │    │    email        │
        │                   │    role         │    │    │    role         │
        │                   └─────────────────┘    │    └─────────────────┘
        │                          │               │            │
        ▼                          │               │            │
┌─────────────────┐                │               │            │
│    courses      │                ▼               │            ▼
├─────────────────┤         ┌─────────────────┐    │    ┌─────────────────┐
│ PK id           │         │ teacher_student │    │    │  student_course │
│    name         │         ├─────────────────┤    │    ├─────────────────┤
│    description  │         │ FK teacher_id   │────┘    │ FK student_id   │
│ FK department_id│         │ FK student_id   │─────────│ FK course_id    │
└─────────────────┘         └─────────────────┘         └─────────────────┘
                                                                │
                                                                │
                            ┌───────────────────────────────────┘
                            │
                            ▼
                    (links to courses.id)
```

### Tables

#### `students`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL | Student's full name |
| `roll` | VARCHAR(255) | NOT NULL, UNIQUE | Roll number (unique identifier) |
| `email` | VARCHAR(255) | | Student's email address |
| `role` | VARCHAR(50) | NOT NULL, DEFAULT 'STUDENT' | Role enum (STUDENT/TEACHER) |

#### `teachers`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL | Teacher's full name |
| `email` | VARCHAR(255) | | Teacher's email address |
| `role` | VARCHAR(50) | NOT NULL, DEFAULT 'TEACHER' | Role enum (STUDENT/TEACHER) |
| `department_id` | BIGINT | FOREIGN KEY → departments(id) | Associated department |

#### `courses`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL | Course name |
| `description` | VARCHAR(500) | | Course description |
| `department_id` | BIGINT | FOREIGN KEY → departments(id) | Department offering this course |

#### `departments`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `name` | VARCHAR(255) | NOT NULL, UNIQUE | Department name |

### Junction Tables (Many-to-Many Relationships)

#### `teacher_student`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `teacher_id` | BIGINT | FOREIGN KEY → teachers(id), PRIMARY KEY | Teacher reference |
| `student_id` | BIGINT | FOREIGN KEY → students(id), PRIMARY KEY | Student reference |

**Relationship:** One teacher can have multiple students, and one student can have multiple teachers.

#### `student_course`
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `student_id` | BIGINT | FOREIGN KEY → students(id), PRIMARY KEY | Student reference |
| `course_id` | BIGINT | FOREIGN KEY → courses(id), PRIMARY KEY | Course reference |

**Relationship:** One student can enroll in multiple courses, and one course can have multiple students.

### Relationships Summary

| Relationship | Type | Owner Side | Description |
|--------------|------|------------|-------------|
| Teacher ↔ Student | ManyToMany | Teacher | Teachers manage students; students can have multiple teachers |
| Student ↔ Course | ManyToMany | Student | Students enroll in courses |
| Department → Teacher | OneToMany | Department | A department has many teachers |
| Department → Course | OneToMany | Department | A department offers many courses |

## 🔐 Security Architecture Details

### Components

```
src/main/java/com/example/webapp/
├── config/
│   └── SecurityConfig.java         # Spring Security configuration
├── security/
│   ├── CustomUserDetails.java      # UserDetails implementation
│   └── CustomUserDetailsService.java # UserDetailsService implementation
├── service/
│   ├── SecurityService.java        # Custom security checks (@securityService)
│   └── UserService.java            # User registration & management
├── entity/
│   └── User.java                   # Authentication user entity
├── repository/
│   └── UserRepository.java         # User data access
└── dto/
    └── RegisterDTO.java            # Registration form validation
```

### Security Flow

```
1. User submits login form (/auth/login)
        │
        ▼
2. Spring Security intercepts request
        │
        ▼
3. CustomUserDetailsService.loadUserByUsername()
        │
        ▼
4. BCrypt compares password hash
        │
        ▼
5. Authentication object created with authorities
        │
        ▼
6. Session created with JSESSIONID cookie
        │
        ▼
7. User redirected to home page
```

### Password Security

- **Algorithm:** BCrypt with strength factor 10
- **Hash Example:** `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z6qMXPrpcCc8Jq5kvuGvDiBi`
- **Rainbow Table Resistant:** Yes (salted)
- **Brute Force Resistant:** Yes (computationally expensive)

### Session Security

| Feature | Configuration |
|---------|---------------|
| Session ID Cookie | `JSESSIONID` (HttpOnly) |
| Maximum Sessions | 1 per user |
| Invalid Session URL | `/auth/login` |
| Logout | Invalidates session, deletes cookie |

### CSRF Protection

- Enabled by default for all state-changing requests
- Token automatically included in Thymeleaf forms via `th:action`
- Logout requires POST with CSRF token

## Docker Configuration

### Services

| Service | Image | Port | Description |
|---------|-------|------|-------------|
| `app` | Custom (Dockerfile) | 8080:8080 | Spring Boot application |
| `postgres` | postgres:16 | 5432:5432 | PostgreSQL database |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/admindb` | Database connection |
| `POSTGRES_DB` | `admindb` | Database name |
| `POSTGRES_USER` | `admin` | Database user |
| `POSTGRES_PASSWORD` | `admin` | Database password |

## License

This project is for educational purposes.
| Teacher → Department | ManyToOne | Teacher | Each teacher belongs to one department |
| Course → Department | ManyToOne | Course | Each course belongs to one department |

### JPA Entity Mappings

```java
// Student.java
@ManyToMany(mappedBy = "students")
private Set<Teacher> teachers;

@ManyToMany
@JoinTable(name = "student_course",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id"))
private Set<Course> courses;

// Teacher.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id")
private Department department;

@ManyToMany
@JoinTable(name = "teacher_student",
    joinColumns = @JoinColumn(name = "teacher_id"),
    inverseJoinColumns = @JoinColumn(name = "student_id"))
private Set<Student> students;

// Course.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "department_id")
private Department department;

@ManyToMany(mappedBy = "courses")
private Set<Student> students;

// Department.java
@OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
private Set<Teacher> teachers;

@OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
private Set<Course> courses;
```

### Role Enum

```java
public enum Role {
    STUDENT,
    TEACHER
}
```

| Role | Description | Permissions |
|------|-------------|-------------|
| `STUDENT` | Student user | View all, edit own profile (except role) |
| `TEACHER` | Teacher user | Full CRUD on all entities |


## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database JDBC URL | `jdbc:postgresql://localhost:5432/admindb` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `admin` |

## License

This project is for educational purposes.
