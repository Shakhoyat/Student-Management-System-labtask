# Student Management System

A Spring Boot web application for managing students, teachers, courses, and departments with role-based authorization.

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
- **Frontend:** Thymeleaf, HTML, CSS
- **Database:** PostgreSQL 16
- **Containerization:** Docker, Docker Compose

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
2. Click **"Login as Teacher"** for full access
3. Click **"Login as Student"** for limited access

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

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Department │     │   Teacher   │     │   Student   │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ id          │◄────│ department_id│     │ id          │
│ name        │     │ id          │◄───►│ name        │
└─────────────┘     │ name        │     │ roll        │
       │            │ email       │     │ email       │
       │            │ role        │     │ role        │
       ▼            └─────────────┘     └─────────────┘
┌─────────────┐            │                   │
│   Course    │            │                   │
├─────────────┤            ▼                   ▼
│ id          │     ┌──────────────┐   ┌──────────────┐
│ name        │     │teacher_student│   │student_course│
│ description │     ├──────────────┤   ├──────────────┤
│ department_id│     │ teacher_id   │   │ student_id   │
└─────────────┘     │ student_id   │   │ course_id    │
                    └──────────────┘   └──────────────┘
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | Database JDBC URL | `jdbc:postgresql://localhost:5432/admindb` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `admin` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `admin` |

## License

This project is for educational purposes.
