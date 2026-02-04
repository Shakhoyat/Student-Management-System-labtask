# Spring Boot Interview Preparation Guide
## Student Management System - Complete Technical Deep Dive

This comprehensive guide will prepare you to explain every aspect of this Spring Boot application in a technical interview. Each section includes code references, explanations, and potential interview questions.

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Spring Boot Architecture](#2-spring-boot-architecture)
3. [Spring Security Authentication & Authorization](#3-spring-security-authentication--authorization)
4. [JPA & Database Relationships](#4-jpa--database-relationships)
5. [Docker & Containerization](#5-docker--containerization)
6. [Frontend-Backend Communication](#6-frontend-backend-communication)
7. [Common Interview Questions](#7-common-interview-questions)

---

## 1. Project Overview

### 1.1 Technology Stack
| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.0.1 | Application framework |
| Spring Security | 7.x | Authentication & Authorization |
| Spring Data JPA | - | Database ORM |
| PostgreSQL | 16 | Database |
| Thymeleaf | - | Server-side templating |
| Docker | - | Containerization |
| Maven | - | Build tool |

### 1.2 Project Structure
```
src/main/java/com/example/webapp/
├── WebappApplication.java      # Entry point
├── config/
│   ├── AppConfig.java          # General configuration
│   └── SecurityConfig.java     # Spring Security configuration
├── controller/                 # HTTP request handlers
├── dto/                        # Data Transfer Objects
├── entity/                     # JPA entities (database tables)
├── repository/                 # Data access layer
├── security/                   # Security components
└── service/                    # Business logic layer
```

### 1.3 Entry Point
**File:** [WebappApplication.java](src/main/java/com/example/webapp/WebappApplication.java)

```java
@SpringBootApplication  // Line 6
public class WebappApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);  // Line 9
    }
}
```

**Interview Explanation:**
- `@SpringBootApplication` is a meta-annotation combining:
  - `@Configuration` - Marks this as a configuration class
  - `@EnableAutoConfiguration` - Enables Spring Boot's auto-configuration
  - `@ComponentScan` - Scans for components in this package and sub-packages
- `SpringApplication.run()` bootstraps the application, creating the ApplicationContext

---

## 2. Spring Boot Architecture

### 2.1 Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                    │
│  Controllers (StudentController, AuthController, etc.)   │
│  Templates (Thymeleaf HTML files)                       │
└─────────────────────────────────────────────────────────┘
                           ↓ ↑
┌─────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                        │
│  Business Logic (StudentService, UserService, etc.)      │
│  @Service, @Transactional                               │
└─────────────────────────────────────────────────────────┘
                           ↓ ↑
┌─────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER                       │
│  Data Access (StudentRepository, UserRepository, etc.)   │
│  JpaRepository interfaces                               │
└─────────────────────────────────────────────────────────┘
                           ↓ ↑
┌─────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                        │
│  PostgreSQL (Entities: Student, Teacher, Course, etc.)   │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Dependency Injection

**File:** [StudentController.java](src/main/java/com/example/webapp/controller/StudentController.java) - Lines 19-28

```java
@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;      // Line 19
    private final CourseService courseService;        // Line 20
    private final TeacherService teacherService;      // Line 21

    // Constructor Injection (recommended approach)
    public StudentController(StudentService studentService,  // Line 23
                            CourseService courseService,
                            TeacherService teacherService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.teacherService = teacherService;
    }
}
```

**Interview Explanation:**
- **Constructor Injection** is preferred over `@Autowired` field injection because:
  1. Makes dependencies explicit
  2. Enables immutability (final fields)
  3. Easier to test (can pass mock dependencies)
  4. Fails fast if dependency is missing

### 2.3 Configuration Properties

**File:** [application.yml](src/main/resources/application.yml)

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://localhost:5432/admindb  # Line 9
    username: admin                                 # Line 10
    password: admin                                 # Line 11

  jpa:
    hibernate:
      ddl-auto: update  # Line 15 - Auto-creates/updates tables

  docker:
    compose:
      enabled: true     # Line 18 - Auto-starts docker-compose
```

**ddl-auto options:**
| Value | Behavior |
|-------|----------|
| `none` | No action |
| `validate` | Validates schema, no changes |
| `update` | Updates schema without data loss |
| `create` | Creates schema, destroys previous data |
| `create-drop` | Creates on startup, drops on shutdown |

---

## 3. Spring Security Authentication & Authorization

### 3.1 Security Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                 HTTP REQUEST                             │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│              SECURITY FILTER CHAIN                       │
│  1. UsernamePasswordAuthenticationFilter                │
│  2. SecurityContextPersistenceFilter                    │
│  3. ExceptionTranslationFilter                          │
│  4. FilterSecurityInterceptor                           │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│           AUTHENTICATION MANAGER                         │
│  DaoAuthenticationProvider → UserDetailsService         │
│  Password verification with BCryptPasswordEncoder       │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│           AUTHORIZATION CHECK                            │
│  @PreAuthorize, hasRole(), URL-based rules              │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│              CONTROLLER / RESOURCE                       │
└─────────────────────────────────────────────────────────┘
```

### 3.2 Security Configuration

**File:** [SecurityConfig.java](src/main/java/com/example/webapp/config/SecurityConfig.java)

```java
@Configuration                                    // Line 16
@EnableWebSecurity                                // Line 17
@EnableMethodSecurity(prePostEnabled = true)      // Line 18 - Enables @PreAuthorize
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;  // Line 21

    // Password Encoder Bean - BCrypt hashing
    @Bean
    public PasswordEncoder passwordEncoder() {    // Line 27-29
        return new BCryptPasswordEncoder();
    }

    // Authentication Provider - connects UserDetailsService + PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {  // Line 32-36
        DaoAuthenticationProvider authProvider = 
            new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
```

**Key Annotations:**
| Annotation | Purpose |
|------------|---------|
| `@EnableWebSecurity` | Enables Spring Security's web security support |
| `@EnableMethodSecurity` | Enables method-level security (`@PreAuthorize`) |

### 3.3 Security Filter Chain

**File:** [SecurityConfig.java](src/main/java/com/example/webapp/config/SecurityConfig.java) - Lines 44-80

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authenticationProvider(authenticationProvider())
        
        // URL-BASED AUTHORIZATION RULES
        .authorizeHttpRequests(auth -> auth
            // Public endpoints - no authentication required
            .requestMatchers("/", "/auth/login", "/auth/register", 
                           "/css/**", "/js/**").permitAll()  // Line 49
            
            // TEACHER-ONLY endpoints
            .requestMatchers("/students/new", "/students/*/delete")
                .hasRole("TEACHER")                          // Line 51
            .requestMatchers("/teachers/new", "/teachers/*/edit", 
                           "/teachers/*/delete").hasRole("TEACHER")
            .requestMatchers("/courses/new", "/courses/*/edit", 
                           "/courses/*/delete").hasRole("TEACHER")
            
            // All other requests require authentication
            .anyRequest().authenticated()                    // Line 56
        )
        
        // FORM LOGIN CONFIGURATION
        .formLogin(form -> form
            .loginPage("/auth/login")           // Custom login page
            .loginProcessingUrl("/auth/login")  // Form POST URL
            .defaultSuccessUrl("/", true)       // Redirect after success
            .failureUrl("/auth/login?error=true")
            .usernameParameter("username")      // Form field names
            .passwordParameter("password")
            .permitAll()
        )
        
        // LOGOUT CONFIGURATION
        .logout(logout -> logout
            .logoutUrl("/auth/logout")
            .logoutSuccessUrl("/auth/login?logout=true")
            .invalidateHttpSession(true)        // Destroy session
            .deleteCookies("JSESSIONID")        // Clear cookies
        );
```

### 3.4 User Entity (Credentials Storage)

**File:** [User.java](src/main/java/com/example/webapp/entity/User.java)

```java
@Entity
@Table(name = "users")                              // Line 8
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                // Line 13

    @Column(nullable = false, unique = true)
    private String username;                        // Line 17 - Login identifier

    @Column(nullable = false)
    private String password;                        // Line 21 - BCrypt hashed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STUDENT;               // Line 30 - STUDENT or TEACHER

    private boolean enabled = true;                 // Line 33 - Account active?

    private Long profileId;                         // Line 36 - Links to Student/Teacher
}
```

**Database Table Created:**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt hash
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(50) NOT NULL,       -- 'STUDENT' or 'TEACHER'
    enabled BOOLEAN DEFAULT true,
    profile_id BIGINT
);
```

### 3.5 UserDetailsService Implementation

**File:** [CustomUserDetailsService.java](src/main/java/com/example/webapp/security/CustomUserDetailsService.java)

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)   // Line 19
            throws UsernameNotFoundException {
        
        // 1. Find user in database
        User user = userRepository.findByUsername(username)  // Line 20-21
                .orElseThrow(() -> 
                    new UsernameNotFoundException("User not found: " + username));
        
        // 2. Wrap in CustomUserDetails (implements UserDetails)
        return new CustomUserDetails(user);                  // Line 23
    }
}
```

**Authentication Flow:**
1. User submits login form with username/password
2. `UsernamePasswordAuthenticationFilter` intercepts request
3. Calls `AuthenticationManager.authenticate()`
4. `DaoAuthenticationProvider` calls `loadUserByUsername()`
5. Compares BCrypt hash of submitted password with stored hash
6. If match → creates `Authentication` object in `SecurityContext`

### 3.6 CustomUserDetails (UserDetails Wrapper)

**File:** [CustomUserDetails.java](src/main/java/com/example/webapp/security/CustomUserDetails.java)

```java
public class CustomUserDetails implements UserDetails {

    private final User user;                               // Line 13

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {  // Line 19-23
        // Convert Role enum to Spring Security authority
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        // Returns: "ROLE_STUDENT" or "ROLE_TEACHER"
    }

    @Override
    public String getPassword() {                          // Line 26
        return user.getPassword();  // BCrypt hash
    }

    @Override
    public String getUsername() {                          // Line 31
        return user.getUsername();
    }

    // Account status methods
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return user.isEnabled(); }
}
```

### 3.7 Method-Level Security (@PreAuthorize)

**File:** [StudentController.java](src/main/java/com/example/webapp/controller/StudentController.java)

```java
// TEACHER-ONLY: Create new student
@GetMapping("/new")
@PreAuthorize("hasRole('TEACHER')")                        // Line 52
public String showAddForm(Model model) {
    // Only users with ROLE_TEACHER can access
}

// TEACHER OR OWN PROFILE: Edit student
@GetMapping("/{id}/edit")
@PreAuthorize("hasRole('TEACHER') or " +
              "(hasRole('STUDENT') and " +
              "@securityService.isOwnProfile(#id, authentication))")  // Line 69
public String showEditForm(@PathVariable Long id, Model model) {
    // Teachers can edit any student
    // Students can only edit their own profile
}

// TEACHER-ONLY: Delete student
@PostMapping("/{id}/delete")
@PreAuthorize("hasRole('TEACHER')")                        // Line 97
public String deleteStudent(@PathVariable Long id) {
    // Only teachers can delete students
}
```

**SpEL (Spring Expression Language) in @PreAuthorize:**
| Expression | Meaning |
|------------|---------|
| `hasRole('TEACHER')` | User has ROLE_TEACHER authority |
| `hasRole('STUDENT')` | User has ROLE_STUDENT authority |
| `#id` | Method parameter named 'id' |
| `authentication` | Current Authentication object |
| `@securityService` | Spring bean reference |

### 3.8 Password Encoding

**File:** [UserService.java](src/main/java/com/example/webapp/service/UserService.java)

```java
@Service
public class UserService {
    
    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        
        // NEVER store plain text passwords!
        // BCrypt generates different hash each time (includes salt)
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        user.setRole(dto.getRole());
        return userRepository.save(user);
    }
}
```

**BCrypt Example:**
```
Plain password: "password123"
BCrypt hash:    "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z6qMXPrpcCc8Jq5kvuGvDiBi"
                 │  │  └─────────────────────────────────────────────────────────┘
                 │  │                        Hash + Salt
                 │  └─ Cost factor (10 = 2^10 iterations)
                 └─ Algorithm identifier (2a = BCrypt)
```

---

## 4. JPA & Database Relationships

### 4.1 Entity-Relationship Diagram

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  DEPARTMENT │       │   TEACHER   │       │   STUDENT   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │──┐    │ id (PK)     │       │ id (PK)     │
│ name        │  │    │ name        │       │ name        │
└─────────────┘  │    │ email       │       │ roll        │
       │         │    │ role        │       │ email       │
       │ 1:M     └───>│ department_id│      │ role        │
       │              └─────────────┘       └─────────────┘
       │                     │                     │
       │                     │ M:M                 │ M:M
       │                     │                     │
       │              ┌──────┴──────┐       ┌──────┴──────┐
       │              │teacher_student      │student_course│
       │              ├─────────────┤       ├─────────────┤
       │              │ teacher_id  │       │ student_id  │
       │              │ student_id  │       │ course_id   │
       │              └─────────────┘       └─────────────┘
       │                                           │
       │ 1:M   ┌─────────────┐                     │
       └──────>│   COURSE    │<────────────────────┘
               ├─────────────┤       M:M
               │ id (PK)     │
               │ name        │
               │ description │
               │department_id│
               └─────────────┘
```

### 4.2 One-to-Many Relationship (1:M)

**Example:** Department → Teachers (One department has many teachers)

**File:** [Department.java](src/main/java/com/example/webapp/entity/Department.java) - Lines 18-19

```java
@Entity
@Table(name = "departments")
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // ONE Department has MANY Teachers
    @OneToMany(mappedBy = "department",      // Line 18
               cascade = CascadeType.ALL)    // Line 18
    private Set<Teacher> teachers = new HashSet<>();  // Line 19
}
```

**File:** [Teacher.java](src/main/java/com/example/webapp/entity/Teacher.java) - Lines 24-26

```java
@Entity
@Table(name = "teachers")
public class Teacher {

    // MANY Teachers belong to ONE Department
    @ManyToOne(fetch = FetchType.LAZY)       // Line 24
    @JoinColumn(name = "department_id")      // Line 25 - Foreign key column
    private Department department;            // Line 26
}
```

**Database Tables:**
```sql
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE teachers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    department_id BIGINT REFERENCES departments(id)  -- Foreign Key
);
```

**Key Concepts:**
| Annotation | Side | Description |
|------------|------|-------------|
| `@OneToMany` | Parent | "I have many children" |
| `@ManyToOne` | Child | "I belong to one parent" |
| `mappedBy` | Parent | Field name in child entity that owns the relationship |
| `@JoinColumn` | Child | Specifies the FK column name |

### 4.3 Many-to-Many Relationship (M:M)

**Example:** Teacher ↔ Student (Teachers can have many students, students can have many teachers)

**File:** [Teacher.java](src/main/java/com/example/webapp/entity/Teacher.java) - Lines 28-33

```java
@Entity
public class Teacher {

    // OWNER side - defines the join table
    @ManyToMany                                          // Line 28
    @JoinTable(
        name = "teacher_student",                        // Line 30 - Join table name
        joinColumns = @JoinColumn(name = "teacher_id"),  // Line 31 - This entity's FK
        inverseJoinColumns = @JoinColumn(name = "student_id")  // Line 32 - Other entity's FK
    )
    private Set<Student> students = new HashSet<>();     // Line 34
}
```

**File:** [Student.java](src/main/java/com/example/webapp/entity/Student.java) - Lines 27-28

```java
@Entity
public class Student {

    // INVERSE side - references the owner's field
    @ManyToMany(mappedBy = "students")                   // Line 27
    private Set<Teacher> teachers = new HashSet<>();     // Line 28
}
```

**Database Tables:**
```sql
-- Join table created automatically by JPA
CREATE TABLE teacher_student (
    teacher_id BIGINT NOT NULL REFERENCES teachers(id),
    student_id BIGINT NOT NULL REFERENCES students(id),
    PRIMARY KEY (teacher_id, student_id)
);
```

### 4.4 Student-Course M:M (Student Owns Relationship)

**File:** [Student.java](src/main/java/com/example/webapp/entity/Student.java) - Lines 30-36

```java
@Entity
public class Student {

    // Student OWNS this relationship
    @ManyToMany                                          // Line 30
    @JoinTable(
        name = "student_course",                         // Line 32
        joinColumns = @JoinColumn(name = "student_id"),  // Line 33
        inverseJoinColumns = @JoinColumn(name = "course_id")  // Line 34
    )
    private Set<Course> courses = new HashSet<>();       // Line 36
}
```

**File:** [Course.java](src/main/java/com/example/webapp/entity/Course.java) - Lines 25-26

```java
@Entity
public class Course {

    // Course is INVERSE side (doesn't own relationship)
    @ManyToMany(mappedBy = "courses")                    // Line 25
    private Set<Student> students = new HashSet<>();     // Line 26
}
```

**Important:** The entity with `@JoinTable` is the **owner**. Only changes to the owner's collection are persisted!

### 4.5 Managing M:M Relationships in Service Layer

**File:** [CourseService.java](src/main/java/com/example/webapp/service/CourseService.java)

```java
@Transactional
public Course updateCourse(Long id, CourseDTO courseDTO) {
    Course course = courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));
    
    course.setName(courseDTO.getName());
    course.setDescription(courseDTO.getDescription());
    
    // CRITICAL: Student owns the relationship!
    // Must update from Student side, not Course side
    
    // 1. Remove course from OLD students
    for (Student oldStudent : new HashSet<>(course.getStudents())) {
        oldStudent.getCourses().remove(course);  // Update owner's collection
    }
    
    // 2. Add course to NEW students
    if (courseDTO.getStudentIds() != null) {
        List<Student> newStudents = studentRepository
                .findAllById(courseDTO.getStudentIds());
        for (Student student : newStudents) {
            student.getCourses().add(course);    // Update owner's collection
        }
    }
    
    return courseRepository.save(course);
}
```

### 4.6 Repository Layer

**File:** [StudentRepository.java](src/main/java/com/example/webapp/repository/StudentRepository.java)

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Spring Data JPA generates SQL from method name
    Optional<Student> findByRoll(String roll);
    // Generated: SELECT * FROM students WHERE roll = ?
    
    Optional<Student> findByEmail(String email);
    // Generated: SELECT * FROM students WHERE email = ?
}
```

**JpaRepository provides:**
| Method | SQL Equivalent |
|--------|----------------|
| `save(entity)` | INSERT or UPDATE |
| `findById(id)` | SELECT * WHERE id = ? |
| `findAll()` | SELECT * |
| `deleteById(id)` | DELETE WHERE id = ? |
| `existsById(id)` | SELECT COUNT(*) WHERE id = ? |

---

## 5. Docker & Containerization

### 5.1 Docker Architecture

```
┌────────────────────────────────────────────────────────┐
│                    HOST MACHINE                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │               DOCKER ENGINE                       │  │
│  │  ┌─────────────────┐    ┌─────────────────────┐  │  │
│  │  │   app container │    │  postgres container │  │  │
│  │  │  ┌───────────┐  │    │  ┌───────────────┐  │  │  │
│  │  │  │Spring Boot│  │───>│  │  PostgreSQL   │  │  │  │
│  │  │  │   :8080   │  │    │  │     :5432     │  │  │  │
│  │  │  └───────────┘  │    │  └───────────────┘  │  │  │
│  │  └────────┬────────┘    └────────┬────────────┘  │  │
│  │           │                      │               │  │
│  │           └──────────┬───────────┘               │  │
│  │                      │                           │  │
│  │              ┌───────┴───────┐                   │  │
│  │              │ Docker Network│                   │  │
│  │              │   (bridge)    │                   │  │
│  │              └───────────────┘                   │  │
│  └──────────────────────────────────────────────────┘  │
│                         │                              │
│                   Port Mapping                         │
│                   8080 → 8080                          │
└─────────────────────────┼──────────────────────────────┘
                          │
                    localhost:8080
```

### 5.2 Dockerfile

**File:** [Dockerfile](Dockerfile)

```dockerfile
# Base image with JDK 17
FROM eclipse-temurin:17-jdk                 # Line 1

# Set working directory inside container
WORKDIR /app                                # Line 2

# Copy the built JAR file into container
COPY target/*.jar app.jar                   # Line 3

# Command to run when container starts
ENTRYPOINT ["java","-jar","app.jar"]        # Line 4
```

**Build Process:**
1. Maven builds `webapp-0.0.1-SNAPSHOT.jar` in `target/` directory
2. Docker COPY instruction copies JAR into container as `app.jar`
3. ENTRYPOINT defines the command to run: `java -jar app.jar`

### 5.3 Docker Compose

**File:** [compose.yaml](compose.yaml)

```yaml
services:
  # PostgreSQL Database Container
  postgres:
    image: postgres:16                      # Line 3 - Official PostgreSQL image
    environment:
      POSTGRES_DB: admindb                  # Line 5 - Database name
      POSTGRES_USER: admin                  # Line 6 - Username
      POSTGRES_PASSWORD: admin              # Line 7 - Password
    volumes:
      - pgdata:/var/lib/postgresql/data     # Line 9 - Persistent storage

  # Spring Boot Application Container
  app:
    build: .                                # Line 12 - Build from Dockerfile
    depends_on:
      - postgres                            # Line 14 - Wait for postgres to start
    environment:
      # Override application.yml settings
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/admindb  # Line 16
      SPRING_DATASOURCE_USERNAME: admin     # Line 17
      SPRING_DATASOURCE_PASSWORD: admin     # Line 18
    ports:
      - "8080:8080"                          # Line 20 - Host:Container port mapping

volumes:
  pgdata:                                   # Line 23 - Named volume for data persistence
```

**Key Concepts:**

| Concept | Explanation |
|---------|-------------|
| `depends_on` | Starts postgres before app (doesn't wait for ready) |
| `volumes` | Persists database data even when container is destroyed |
| `environment` | Override Spring Boot properties (SPRING_* maps to spring.*) |
| `postgres:5432` | Container hostname (Docker DNS resolves service names) |

### 5.4 Spring Boot Docker Integration

**File:** [pom.xml](pom.xml) - Lines 65-69

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-docker-compose</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**File:** [application.yml](src/main/resources/application.yml) - Lines 17-19

```yaml
spring:
  docker:
    compose:
      enabled: true    # Auto-starts docker-compose when app runs
```

**What this does:**
- When you run the app locally (not in Docker), Spring Boot automatically runs `docker-compose up`
- Starts PostgreSQL container before the app connects
- Reads `compose.yaml` file in project root

### 5.5 Docker Commands Reference

```bash
# Build and start containers
docker-compose up --build -d

# View running containers
docker ps

# View logs
docker-compose logs -f app

# Stop containers
docker-compose down

# Stop and remove volumes (deletes database data!)
docker-compose down -v

# Rebuild only app container
docker-compose build app
docker-compose up -d
```

---

## 6. Frontend-Backend Communication

### 6.1 Request-Response Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         BROWSER                                  │
│                                                                 │
│  1. User clicks "Add Student" button                            │
│     GET /students/new                                           │
└────────────────────────────────────────────────────────────────┬┘
                                                                 │
                              HTTP Request                       │
                                                                 ↓
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING SECURITY                              │
│                                                                 │
│  2. Check: Is user authenticated? ✓                             │
│  3. Check: Does user have ROLE_TEACHER? ✓                       │
└────────────────────────────────────────────────────────────────┬┘
                                                                 │
                                                                 ↓
┌─────────────────────────────────────────────────────────────────┐
│                    CONTROLLER                                   │
│  StudentController.showAddForm()                                │
│                                                                 │
│  4. Create empty StudentDTO                                     │
│  5. Add to Model                                                │
│  6. Return view name: "student-form"                            │
└────────────────────────────────────────────────────────────────┬┘
                                                                 │
                                                                 ↓
┌─────────────────────────────────────────────────────────────────┐
│                    THYMELEAF                                    │
│                                                                 │
│  7. Find template: templates/student-form.html                  │
│  8. Process th:* attributes                                     │
│  9. Generate final HTML                                         │
└────────────────────────────────────────────────────────────────┬┘
                                                                 │
                              HTTP Response (HTML)               │
                                                                 ↓
┌─────────────────────────────────────────────────────────────────┐
│                         BROWSER                                  │
│                                                                 │
│  10. Render HTML form                                           │
│  11. User fills form, clicks Submit                             │
│      POST /students                                             │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Controller → View Data Flow

**File:** [StudentController.java](src/main/java/com/example/webapp/controller/StudentController.java) - Lines 52-58

```java
@GetMapping("/new")
@PreAuthorize("hasRole('TEACHER')")
public String showAddForm(Model model) {
    
    // 1. Add empty DTO for form binding
    model.addAttribute("student", new StudentDTO());       // Line 55
    
    // 2. Add lists for dropdown selections
    model.addAttribute("teachers", teacherService.getAllTeachersDTO());  // Line 56
    model.addAttribute("courses", courseService.getAllCoursesDTO());     // Line 57
    
    // 3. Return view name (resolves to templates/student-form.html)
    return "student-form";                                  // Line 58
}
```

**File:** [student-form.html](src/main/resources/templates/student-form.html)

```html
<!-- Form binds to StudentDTO object -->
<form th:action="@{/students}" th:object="${student}" method="post">
    
    <!-- th:field binds to StudentDTO.name -->
    <input type="text" th:field="*{name}" class="form-control" required>
    
    <!-- th:field binds to StudentDTO.roll -->
    <input type="text" th:field="*{roll}" class="form-control" required>
    
    <!-- th:field binds to StudentDTO.email -->
    <input type="email" th:field="*{email}" class="form-control">
    
    <!-- Dropdown populated from teachers list -->
    <select th:field="*{teacherIds}" multiple class="form-control">
        <option th:each="teacher : ${teachers}" 
                th:value="${teacher.id}" 
                th:text="${teacher.name}">Teacher</option>
    </select>
</form>
```

### 6.3 Form Submission → Controller

**File:** [StudentController.java](src/main/java/com/example/webapp/controller/StudentController.java) - Lines 60-66

```java
@PostMapping                                               // Handles POST /students
@PreAuthorize("hasRole('TEACHER')")
public String createStudent(
        @ModelAttribute("student") StudentDTO studentDTO,  // Form data bound to DTO
        RedirectAttributes redirectAttributes) {
    
    // 1. Call service to save
    studentService.saveStudent(studentDTO);                // Line 64
    
    // 2. Add flash message (survives redirect)
    redirectAttributes.addFlashAttribute("successMessage", 
                                        "Student created successfully");
    
    // 3. Redirect to list page (PRG pattern)
    return "redirect:/students";                           // Line 66
}
```

**POST-REDIRECT-GET (PRG) Pattern:**
1. User POSTs form data
2. Server processes and saves
3. Server returns REDIRECT response
4. Browser GETs the redirect URL
5. **Prevents duplicate form submission on refresh**

### 6.4 Thymeleaf Syntax Reference

| Syntax | Purpose | Example |
|--------|---------|---------|
| `th:text` | Set text content | `<span th:text="${user.name}">Name</span>` |
| `th:href` | Set URL | `<a th:href="@{/students/{id}(id=${s.id})}">` |
| `th:if` | Conditional render | `<div th:if="${error}">Error!</div>` |
| `th:each` | Loop | `<tr th:each="s : ${students}">` |
| `th:field` | Form binding | `<input th:field="*{name}">` |
| `th:object` | Form object | `<form th:object="${student}">` |
| `th:action` | Form action URL | `<form th:action="@{/students}">` |
| `@{...}` | URL expression | `@{/css/style.css}` |
| `${...}` | Variable expression | `${student.name}` |
| `*{...}` | Selection expression | `*{name}` (from th:object) |

### 6.5 Spring Security in Templates

**File:** [students.html](src/main/resources/templates/students.html)

```html
<!-- Namespace declaration -->
<html xmlns:th="http://www.thymeleaf.org" 
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<!-- Show only if authenticated -->
<div sec:authorize="isAuthenticated()">
    <span sec:authentication="name">Username</span>
</div>

<!-- Show only to TEACHER role -->
<a sec:authorize="hasRole('TEACHER')" 
   th:href="@{/students/new}">Add Student</a>

<!-- Show only to STUDENT role -->
<div sec:authorize="hasRole('STUDENT')">
    You can only edit your own profile.
</div>
```

### 6.6 Data Transfer Objects (DTOs)

**Purpose:** Decouple entity structure from API/view representation

**File:** [StudentDTO.java](src/main/java/com/example/webapp/dto/StudentDTO.java)

```java
public class StudentDTO {
    private Long id;
    private String name;
    private String roll;
    private String email;
    private String role;              // String instead of enum for view
    private Set<Long> teacherIds;     // Just IDs, not full objects
    private Set<Long> courseIds;      // Just IDs, not full objects
    
    // Getters and setters...
}
```

**Why DTOs?**
1. **Security**: Don't expose entity internals (passwords, etc.)
2. **Performance**: Send only needed data (no lazy loading issues)
3. **Flexibility**: View structure can differ from database structure
4. **Validation**: DTOs can have view-specific validation rules

---

## 7. Common Interview Questions

### 7.1 Spring Boot Questions

**Q: What does @SpringBootApplication do?**
> It's a meta-annotation combining @Configuration, @EnableAutoConfiguration, and @ComponentScan. It marks the main class and enables Spring Boot's auto-configuration based on classpath dependencies.

**Q: Explain constructor injection vs field injection.**
> Constructor injection (used in this project) is preferred because:
> - Dependencies are explicit and immutable (final)
> - Class cannot be instantiated without dependencies (fails fast)
> - Easier to unit test (pass mocks in constructor)
> - No reflection needed

**Q: What is the difference between @Controller and @RestController?**
> - `@Controller` returns view names (for Thymeleaf templates)
> - `@RestController` = `@Controller` + `@ResponseBody` (returns JSON/XML directly)
> - This project uses `@Controller` for server-side rendering

### 7.2 Spring Security Questions

**Q: How does Spring Security authenticate users?**
> 1. User submits username/password via login form
> 2. `UsernamePasswordAuthenticationFilter` intercepts the request
> 3. `AuthenticationManager` delegates to `DaoAuthenticationProvider`
> 4. `UserDetailsService.loadUserByUsername()` fetches user from database
> 5. `PasswordEncoder.matches()` compares BCrypt hashes
> 6. On success, `Authentication` object is stored in `SecurityContext`

**Q: What's the difference between authentication and authorization?**
> - **Authentication**: Verifying WHO the user is (login)
> - **Authorization**: Verifying WHAT the user can do (permissions)
> - This project: Authentication via login form, authorization via `@PreAuthorize`

**Q: How does @PreAuthorize work?**
> It uses Spring AOP to intercept method calls. Before the method executes, it evaluates the SpEL expression. If false, throws `AccessDeniedException`. Requires `@EnableMethodSecurity`.

**Q: Why use BCrypt for passwords?**
> - One-way hash (cannot be reversed)
> - Includes salt (protects against rainbow tables)
> - Configurable work factor (can be made slower as hardware improves)
> - Each hash is unique even for same password

### 7.3 JPA Questions

**Q: Explain the difference between @OneToMany and @ManyToOne.**
> - `@OneToMany` on parent: "I have many children" (Department has many Teachers)
> - `@ManyToOne` on child: "I belong to one parent" (Teacher belongs to Department)
> - The `@ManyToOne` side typically owns the relationship (has the FK column)

**Q: What is the "owning side" in JPA relationships?**
> The side that contains the foreign key. In ManyToMany, it's the side with `@JoinTable`. In ManyToOne/OneToMany, it's the ManyToOne side. Only changes to the owning side are persisted.

**Q: What does "mappedBy" mean?**
> It indicates this side does NOT own the relationship. The string value is the field name in the other entity that owns it. Changes to this collection are ignored.

**Q: Explain FetchType.LAZY vs EAGER.**
> - LAZY: Load related entities only when accessed (default for collections)
> - EAGER: Load related entities immediately with parent
> - LAZY is preferred for performance but can cause LazyInitializationException outside transaction

**Q: What is N+1 problem?**
> When fetching a list of entities, each related collection triggers a separate query. Example: 10 students with teachers = 1 query for students + 10 queries for teachers. Solution: Use `JOIN FETCH` or `@EntityGraph`.

### 7.4 Docker Questions

**Q: What's the difference between Docker image and container?**
> - Image: A read-only template (like a class)
> - Container: A running instance of an image (like an object)
> - Multiple containers can run from the same image

**Q: What does docker-compose do?**
> Orchestrates multiple containers. Defines services, networks, and volumes in YAML. Handles startup order, networking between containers, and environment configuration.

**Q: How do containers communicate?**
> Through Docker networks. In compose, all services are on the same bridge network by default. Service names become DNS hostnames (app can reach database via `postgres:5432`).

**Q: What's a Docker volume?**
> Persistent storage that survives container restarts. In this project, `pgdata` volume stores PostgreSQL data, so database survives `docker-compose down`.

### 7.5 Architecture Questions

**Q: Explain the layers in this application.**
> 1. **Controller Layer**: Handles HTTP requests, validates input, returns views
> 2. **Service Layer**: Business logic, transactions, converts between DTO/Entity
> 3. **Repository Layer**: Data access, extends JpaRepository
> 4. **Entity Layer**: JPA entities mapped to database tables

**Q: What is the DTO pattern and why use it?**
> DTO (Data Transfer Object) separates internal entity structure from external representation. Benefits:
> - Security (don't expose sensitive fields)
> - Performance (only send needed data)
> - Decoupling (change entity without breaking API)

**Q: What is @Transactional and when to use it?**
> Marks method/class for database transaction management. Use for:
> - Multiple database operations that should succeed/fail together
> - Operations that modify data
> - Lazy loading outside of repository (keeps session open)

---

## Quick Reference Card

### Key Files
| File | Purpose |
|------|---------|
| `WebappApplication.java` | Application entry point |
| `SecurityConfig.java` | Security rules and authentication |
| `CustomUserDetailsService.java` | Loads users from database |
| `StudentController.java` | HTTP endpoints for students |
| `StudentService.java` | Business logic for students |
| `StudentRepository.java` | Database operations |
| `Student.java` | JPA entity (database table) |
| `StudentDTO.java` | Data transfer object |
| `application.yml` | Configuration properties |
| `compose.yaml` | Docker multi-container setup |

### Key Annotations
| Annotation | Layer | Purpose |
|------------|-------|---------|
| `@SpringBootApplication` | Main | Bootstrap application |
| `@Controller` | Controller | HTTP request handler |
| `@Service` | Service | Business logic component |
| `@Repository` | Repository | Data access component |
| `@Entity` | Entity | JPA database table |
| `@PreAuthorize` | Controller | Method-level security |
| `@Transactional` | Service | Transaction management |

---

*Good luck with your interview! Remember: Understanding WHY things work is more important than memorizing HOW.*
