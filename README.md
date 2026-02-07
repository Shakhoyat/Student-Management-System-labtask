# Student Management System - Spring Boot

A full-stack web application built with **Spring Boot 4.0.1**, **Thymeleaf**, **Spring Security**, **JPA/Hibernate**, and **PostgreSQL**, containerized with **Docker**.

---

## Table of Contents
- [Project Architecture](#project-architecture)
- [How to Run](#how-to-run)
- [Core Spring Boot Concepts](#core-spring-boot-concepts)
- [MVC Architecture](#mvc-architecture)
- [Entity & JPA Concepts](#entity--jpa-concepts)
- [DTO Pattern](#dto-pattern)
- [Service Layer](#service-layer)
- [Repository Layer](#repository-layer)
- [Spring Security](#spring-security)
- [Authentication vs Authorization](#authentication-vs-authorization)
- [Unit Testing](#unit-testing)
- [Docker & Docker Compose](#docker--docker-compose)
- [CI/CD - GitHub Actions](#cicd---github-actions)
- [Branch Protection Rules](#branch-protection-rules)
- [File-by-File Explanation](#file-by-file-explanation)
- [Viva One-Liners](#viva-one-liners)

---

## Project Architecture

```
Controller (HTTP Requests) → Service (Business Logic) → Repository (Database) → Entity (Table)
     ↕                            ↕
   View (Thymeleaf HTML)        DTO (Data Transfer)
```

### Folder Structure
```
src/main/java/com/example/webapp/
├── config/          → Configuration classes (Security, App config)
├── controller/      → Handles HTTP requests (GET, POST)
├── dto/             → Data Transfer Objects (form data carriers)
├── entity/          → JPA entities (maps to database tables)
├── repository/      → Database access layer (JPA queries)
├── security/        → Custom authentication (UserDetails, UserDetailsService)
├── service/         → Business logic layer
src/main/resources/
├── application.yml  → App configuration (DB, JPA, Docker settings)
├── data.sql         → Initial data loaded on startup
├── templates/       → Thymeleaf HTML templates
├── static/css/      → CSS stylesheets
```

---

## How to Run

### Option 1: Docker Compose (Recommended)
```bash
# Build JAR file
./mvnw clean package -DskipTests

# Start PostgreSQL + App
docker-compose up --build
```
App available at: **http://localhost:8080**

### Option 2: Local Development
```bash
# Start only PostgreSQL
docker-compose up postgres -d

# Run Spring Boot app
./mvnw spring-boot:run
```

### Run Tests
```bash
./mvnw test
```

---

## Core Spring Boot Concepts

### What is Spring Boot?
Spring Boot is a framework that simplifies Spring application development by providing auto-configuration, embedded servers, and opinionated defaults.

**Banglish:** Spring Boot hocche ekta framework ja Spring application develop korar process onek easy kore dey. Tumi manually kichu configure na korleo Spring Boot nijer theke configure kore ney.

### Key Annotations
| Annotation | Purpose |
|---|---|
| `@SpringBootApplication` | Main entry point — combines @Configuration + @EnableAutoConfiguration + @ComponentScan |
| `@Configuration` | Class that defines Spring beans |
| `@Bean` | Method that creates a managed object in Spring IoC container |
| `@Service` | Marks a class as business logic layer |
| `@Controller` | Marks a class as web controller handling HTTP requests |
| `@Repository` | Marks a class as data access layer |
| `@Autowired` / Constructor DI | Spring automatically injects dependencies |

### IoC Container (Inversion of Control)

**Banglish:** IoC Container hocche Spring-er core — she object lifecycle manage kore (create, inject, destroy). Developer nijei object create kore na, Spring automatic handle kore. `@Bean` annotation diye container-e object register kora hoy, ar container automatically dependencies inject kore dei. Eita-i hocche Dependency Injection.

```
Traditional:   Developer creates objects manually → new StudentService()
Spring IoC:    Container creates & injects automatically → @Service, @Autowired
```

### Dependency Injection Types
```java
// 1. Constructor Injection (BEST PRACTICE - used in this project)
// Container automatically finds matching bean and passes it
public SecurityConfig(CustomUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
}

// 2. Field Injection (not recommended for production)
@Autowired
private CustomUserDetailsService userDetailsService;
```

---

## MVC Architecture

### Model-View-Controller Pattern
```
User Browser → Controller → Service → Repository → Database
                  ↓
              View (HTML)
```

**Banglish:** MVC hocche ekta design pattern — Model (data represent kore), View (user ke UI dekhay), Controller (HTTP request handle kore ar logic process kore). Ei project-e Thymeleaf hocche View engine — Java data diye HTML generate kore.

### Controller Example Flow
1. User visits `/students` → `StudentController.listStudents()` called
2. Controller calls `StudentService.getAllStudentsDTO()` 
3. Service calls `StudentRepository.findAll()`
4. Repository executes SQL query on PostgreSQL
5. Data flows back: Entity → DTO → Model → Thymeleaf template → HTML → Browser

### Key Controller Annotations
| Annotation | Purpose |
|---|---|
| `@GetMapping("/students")` | Handles GET requests to /students |
| `@PostMapping("/students/new")` | Handles POST (form submission) |
| `@PathVariable` | Extracts value from URL: `/students/{id}` |
| `@ModelAttribute` | Binds form data to DTO object |
| `@Valid` | Triggers validation on DTO fields |

---

## Entity & JPA Concepts

### What is JPA?
JPA (Java Persistence API) is a specification for ORM (Object-Relational Mapping) that maps Java objects to database tables. **Hibernate** is the implementation.

**Banglish:** JPA hocche ekta specification ja Java object ke database table-e map kore. Tumi SQL manually na likhei Java code diye database operate korte paro. Hibernate hocche JPA-r real implementation — she SQL generate kore ar database e send kore.

### Entity = Database Table
```java
@Entity                                    // Marks as DB table
@Table(name = "students")                  // Table name in PostgreSQL
public class Student {
    @Id                                     // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(nullable = false, unique = true) // Column constraints
    private String roll;

    @Enumerated(EnumType.STRING)            // Store enum as string (not number)
    private Role role;
}
```

### Relationships
| Annotation | Meaning | Example |
|---|---|---|
| `@OneToMany` | One department has many teachers | Department → Teachers |
| `@ManyToOne` | Many teachers belong to one department | Teacher → Department |
| `@ManyToMany` | Students enroll in many courses, courses have many students | Student ↔ Course |
| `@JoinTable` | Creates junction table for ManyToMany | student_course table |
| `mappedBy` | The OTHER side owns the relationship | Non-owning side |

### Entity Relationships in This Project
```
Department ──OneToMany──→ Teacher
Department ──OneToMany──→ Course
Teacher    ──ManyToMany──→ Student   (teacher_student junction table)
Student    ──ManyToMany──→ Course    (student_course junction table)
```

**Banglish:** Relationship mane entity-gulo kibhabe connected. `@OneToMany` mane ek Department-er onek Teacher thakte pare. `@ManyToMany`-te ekta junction table toiri hoy (student_course), karon dui dike multiple relationship thake. `mappedBy` mane oi relationship-er owner onyo side — relationship duplicate hobe na.

### DDL-Auto Modes
```yaml
jpa:
  hibernate:
    ddl-auto: update  # Options: create, create-drop, update, validate, none
```
| Mode | Purpose |
|---|---|
| `create` | Drop and recreate tables every start |
| `create-drop` | Create on start, drop on shutdown |
| `update` | Update schema without losing data (dev) |
| `validate` | Only validate, don't change schema (prod) |
| `none` | Do nothing |

---

## DTO Pattern

### What is DTO?
DTO (Data Transfer Object) carries data between layers without exposing the full entity.

### Why DTO?
1. **Security**: Entity has fields we don't want to expose (e.g., password hash)
2. **Decoupling**: View layer doesn't depend on entity structure
3. **Flexibility**: DTO can combine data from multiple entities
4. **Validation**: DTOs can have validation annotations (`@NotBlank`, `@Email`)

**Banglish:** DTO hocche ekta simple class ja data carry kore layer theke layer-e. Entity directly user-er kache pathano risky — karon password ba internal fields expose hoye jete pare. DTO diye amra just dorkar moto data filter kore pathay.

### Example Flow
```
Form Data → DepartmentDTO → Service (converts to Entity) → Repository → Database
Database → Repository → Entity → Service (converts to DTO) → Controller → View
```

### ModelMapper
```java
// Converts DTO to Entity automatically (field names must match)
Department department = modelMapper.map(departmentDTO, Department.class);

// Converts Entity to DTO
DepartmentDTO dto = modelMapper.map(department, DepartmentDTO.class);
```

---

## Service Layer

### What is Service Layer?
Contains business logic between Controller and Repository. Handles data transformation, validation, and transaction management.

**Banglish:** Service layer hocche business logic er ghor — Controller theke request ashe, Service logic process kore, Repository ke database call korte bole. Controller e directly database access kora bad practice.

### Key Annotations
| Annotation | Purpose |
|---|---|
| `@Service` | Marks class as service bean (Spring manages it) |
| `@Transactional` | Wraps method in DB transaction (rollback on error) |

### Services in This Project
| Service | Responsibilities |
|---|---|
| `DepartmentService` | CRUD for departments + DTO conversion |
| `StudentService` | CRUD for students + course assignment + role-based editing |
| `TeacherService` | CRUD for teachers + student assignment |
| `CourseService` | CRUD for courses + student enrollment |
| `UserService` | Registration + password encoding + user management |

---

## Repository Layer

### What is Repository?
Interface extending `JpaRepository` — Spring Data JPA automatically provides CRUD implementations at runtime. No code needed!

**Banglish:** Repository hocche just ekta interface — tumi interface declare koro, Spring nijer theke implementation create kore dey runtime-e. findAll(), save(), deleteById() — shob free!

### Built-in Methods (FREE — no code needed!)
```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    // findAll(), findById(), save(), deleteById(), count() → ALREADY AVAILABLE!
}
```

### Custom Query Methods (Spring generates SQL from method name!)
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // SELECT * FROM users WHERE username = ?
    boolean existsByUsername(String username);        // Returns true/false
}
```

---

## Spring Security

### Security Flow
```
HTTP Request → Security Filter Chain → Authentication → Authorization → Controller
```

### SecurityConfig.java Breakdown
```java
@Configuration           // Spring configuration class
@EnableWebSecurity       // Activate Spring Security
@EnableMethodSecurity    // Allow @PreAuthorize on methods
```

### Key Components
| Component | Purpose |
|---|---|
| `PasswordEncoder` (BCrypt) | Hashes passwords — one-way, can't reverse |
| `DaoAuthenticationProvider` | Authenticates users against database |
| `SecurityFilterChain` | Defines URL access rules, login/logout config |
| `CustomUserDetailsService` | Loads user from database for authentication |
| `CustomUserDetails` | Wraps User entity to implement Spring's `UserDetails` interface |

### Password Storage & Matching
```
Registration:
  Plain password "hello123"
    → BCryptPasswordEncoder.encode("hello123")
    → "$2a$10$xyz..." (stored in DB, this is the HASH)

Login:
  User types "hello123"
    → BCryptPasswordEncoder.matches("hello123", "$2a$10$xyz...")
    → true → Login success!

  User types "wrong"
    → BCryptPasswordEncoder.matches("wrong", "$2a$10$xyz...")
    → false → Login failed!
```

**Banglish:** BCrypt hocche one-way hashing — password hash kore store kore, kono bhabe reverse kora jay na. Login-er somoy user-er typed password ar DB-er hash ke compare kore — match korle success, na korle fail.

---

## Authentication vs Authorization

### Authentication (WHO are you? — তুমি কে?)
- **WHERE**: `DaoAuthenticationProvider` + `CustomUserDetailsService`
- **HOW**: User submits username/password → Spring loads user from DB → compares hashed passwords
- **WHEN**: Login form submission (`/auth/login`)

### Authorization (WHAT can you do? — তুমি কী করতে পারো?)
- **WHERE**: `SecurityFilterChain` → `authorizeHttpRequests()`
- **HOW**: Checks user's role against URL rules after authentication
- **WHEN**: Every HTTP request

### Access Control Rules
```java
.requestMatchers("/students/new").hasRole("TEACHER")  // Only TEACHER can access
.anyRequest().authenticated()                          // Any logged-in user
.requestMatchers("/auth/login").permitAll()             // Everyone (no login needed)
```

### Role-Based Access in This Project
| Action | STUDENT | TEACHER |
|---|---|---|
| View students/teachers/courses | ✅ | ✅ |
| Create/Delete students | ❌ | ✅ |
| Edit own profile | ✅ | ✅ |
| Create/Edit/Delete courses | ❌ | ✅ |
| Create/Edit/Delete departments | ❌ | ✅ |

---

## Unit Testing

### What is Unit Testing?
Testing individual units (methods/classes) **in isolation** without external dependencies (database, network).

**Banglish:** Unit testing mane ekta specific method ba class k alada bhabe test kora — real database ba network lagbe na. Mock object diye dependency fake kore dewa hoy.

### Testing Stack
| Tool | Purpose |
|---|---|
| **JUnit 5** | Test framework — provides `@Test`, assertions |
| **Mockito** | Mocking framework — creates fake objects |
| `@ExtendWith(MockitoExtension)` | Enables Mockito in JUnit |

### Key Annotations
```java
@Mock                   // Creates fake object (no real DB call happens)
private StudentRepository studentRepository;

@InjectMocks            // Creates real service, injects mocks into it
private StudentService studentService;

@BeforeEach             // Runs before EACH test method (setup)
void setUp() { ... }
```

### Test Pattern: Arrange-Act-Assert (AAA)
```java
@Test
void getAllStudents_ShouldReturnList() {
    // ARRANGE: Setup mock behavior (when X is called, return Y)
    when(studentRepository.findAll()).thenReturn(Arrays.asList(student));

    // ACT: Call the actual method being tested
    List<Student> result = studentService.getAllStudents();

    // ASSERT: Verify the result is what we expected
    assertEquals(1, result.size());
    verify(studentRepository, times(1)).findAll();  // Verify mock was called once
}
```

### Common Mockito Methods
| Method | Purpose |
|---|---|
| `when(...).thenReturn(...)` | Defines mock behavior (when X called, return Y) |
| `verify(mock, times(n))` | Checks if mock method was called exactly n times |
| `assertThrows(Exception.class, ...)` | Tests that an exception is thrown |
| `assertEquals(expected, actual)` | Checks if two values are equal |
| `assertNotNull(value)` | Checks if value is not null |

### Test Coverage in This Project (61 Tests, All Passing ✅)
| Service | Tests | What's Tested |
|---|---|---|
| `DepartmentServiceTest` | 7 | CRUD + not-found exceptions |
| `StudentServiceTest` | 14 | CRUD + course assignment + self-edit restriction |
| `TeacherServiceTest` | 14 | CRUD + student assignment + department linking |
| `CourseServiceTest` | 13 | CRUD + student enrollment + department linking |
| `UserServiceTest` | 13 | Registration + password encoding + duplicate check |
| **Total** | **61** | All service layer methods |

### Running Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=DepartmentServiceTest

# Run specific test method
./mvnw test -Dtest=DepartmentServiceTest#getAllDepartments_ShouldReturnList
```

---

## Docker & Docker Compose

### What is Docker?
Docker packages applications into **containers** — isolated, portable environments that run the same everywhere.

**Banglish:** Docker hocche container technology — application ke package kore isolated environment-e run koray, jar fole "jeta amar machine-e cholche" sei problem thake na. Shob jayga-y same bhabe run hobe.

### Dockerfile Explained
```dockerfile
FROM eclipse-temurin:17-jdk    # Base image — JDK 17 installed environment
WORKDIR /app                   # Working directory inside container
COPY target/*.jar app.jar      # Copy built JAR into container
ENTRYPOINT ["java","-jar","app.jar"]  # Command to run when container starts
```

| Instruction | Purpose |
|---|---|
| `FROM` | Base image to start from (like OS + JDK) |
| `WORKDIR` | Sets working directory inside container |
| `COPY` | Copies files from host machine to container |
| `ENTRYPOINT` | Command that runs when container starts |

### Docker Compose Explained
```yaml
services:
  postgres:                     # Database service
    image: postgres:16          # Official PostgreSQL 16 image
    environment:                # Environment variables (DB credentials)
      POSTGRES_DB: admindb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
    volumes:
      - pgdata:/var/lib/postgresql/data  # Persistent storage (data survives restart)

  app:                          # Spring Boot app service
    build: .                    # Build from Dockerfile in current directory
    depends_on: [postgres]      # Start after PostgreSQL is ready
    ports: ["8080:8080"]        # Map host port 8080 to container port 8080
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/admindb
```

### Key Docker Concepts
| Concept | Purpose |
|---|---|
| **Image** | Read-only template for creating containers (like a class) |
| **Container** | Running instance of an image (like an object) |
| **Volume** | Persistent storage that survives container deletion |
| **Port Mapping** | Maps host port to container port (`host:container`) |
| **depends_on** | Defines service startup order |
| **environment** | Sets environment variables inside container |

### Docker Commands
```bash
docker-compose up --build      # Build images & start all services
docker-compose up -d           # Start in background (detached mode)
docker-compose down            # Stop & remove containers
docker-compose logs -f         # View logs in real-time
docker ps                      # List running containers
docker exec -it <name> bash    # Enter a running container
```

---

## CI/CD - GitHub Actions

### What is CI/CD?
- **CI (Continuous Integration)**: Automatically build and test code on every push/PR
- **CD (Continuous Deployment)**: Automatically deploy after tests pass

**Banglish:** CI mane protibar code push korle ba PR create korle automatically build ar test hobe. Kono bug thakle shuru-tei ber hoye jabe — merge korar agei!

### Our CI Pipeline (`.github/workflows/ci.yml`)
```
Push to main / Open PR
    → GitHub triggers workflow
    → Ubuntu VM starts
    → Checkout code
    → Install JDK 17
    → Cache Maven dependencies
    → Run ./mvnw clean verify (compile + test)
    → Pass ✅ or Fail ❌ (upload test reports on failure)
```

### Pipeline Steps
| Step | What Happens |
|---|---|
| `actions/checkout@v4` | Downloads repo code into GitHub VM |
| `actions/setup-java@v4` | Installs JDK 17 (Temurin) + caches Maven |
| `./mvnw clean verify` | Compiles code + runs all 61 tests |
| `upload-artifact` | Saves test reports if tests fail (for debugging) |

### Why CI Matters
1. **Catch bugs early**: Tests run on every push — bugs found immediately
2. **Consistent builds**: Same Ubuntu + JDK 17 environment every time
3. **Gate merging**: Can block PR merge if tests fail (with branch protection)
4. **Team confidence**: Everyone knows the code is tested before merging

---

## Branch Protection Rules

### What are Branch Protection Rules?
Rules on GitHub that prevent direct pushes to important branches (like `main`), ensuring code goes through review and CI before merging.

**Banglish:** Branch protection mane `main` branch-e keu directly push korte parbe na. First feature branch-e kaj korte hobe, PR create korte hobe, CI pass korte hobe, ar teammate review korle tarpor merge hobe.

### How to Set Up (GitHub)
1. Go to **Repository → Settings → Branches**
2. Click **"Add branch protection rule"**
3. Branch name pattern: `main`
4. Enable these rules:

| Rule | Purpose |
|---|---|
| ✅ **Require a pull request before merging** | No direct push to main |
| ✅ **Require approvals** (1+) | At least 1 teammate must review |
| ✅ **Require status checks to pass** | CI (build-and-test) must pass before merge |
| ✅ **Require branches to be up to date** | Branch must have latest main changes |
| ✅ **Require conversation resolution** | All review comments must be resolved |

### Git Workflow with Branch Protection
```bash
# 1. Create feature branch from main
git checkout -b feature/add-enrollment

# 2. Make changes & commit
git add .
git commit -m "Add enrollment feature"

# 3. Push feature branch to GitHub
git push origin feature/add-enrollment

# 4. Create Pull Request on GitHub (main ← feature/add-enrollment)
# 5. GitHub Actions CI runs automatically → Tests must pass ✅
# 6. Teammate reviews code & approves ✅
# 7. All checks passed → Merge to main!
```

### Why This Matters for Teams
```
Without branch protection:
  Developer pushes broken code → main is broken → everyone is blocked

With branch protection:
  Developer pushes broken code → CI catches it → PR blocked → main stays safe
```

---

## File-by-File Explanation

### Configuration Files
| File | Purpose |
|---|---|
| `pom.xml` | Maven project config — dependencies, plugins, Java version, build settings |
| `application.yml` | Spring Boot config — DB connection, JPA settings, Docker compose profile |
| `compose.yaml` | Docker Compose — defines PostgreSQL + Spring Boot app services |
| `Dockerfile` | Instructions to build Docker image for the app |
| `.gitattributes` | Git line ending rules (LF for Unix, CRLF for Windows) |
| `mvnw` / `mvnw.cmd` | Maven wrapper scripts (Unix/Mac / Windows) — no Maven install needed |
| `data.sql` | SQL file that runs on startup to seed initial data |
| `.github/workflows/ci.yml` | GitHub Actions CI pipeline definition |

### Java Classes
| Class | Layer | Purpose |
|---|---|---|
| `WebappApplication.java` | Entry | `main()` method — starts Spring Boot application |
| `SecurityConfig.java` | Config | Security rules, password encoder, form login, session management |
| `AppConfig.java` | Config | ModelMapper bean and other application-wide beans |
| `AuthController.java` | Controller | Login, register, home page endpoints |
| `StudentController.java` | Controller | Student CRUD endpoints (`/students/*`) |
| `TeacherController.java` | Controller | Teacher CRUD endpoints (`/teachers/*`) |
| `CourseController.java` | Controller | Course CRUD endpoints (`/courses/*`) |
| `DepartmentController.java` | Controller | Department CRUD endpoints (`/departments/*`) |
| `StudentDTO.java` | DTO | Student form data carrier with validation |
| `TeacherDTO.java` | DTO | Teacher form data carrier |
| `CourseDTO.java` | DTO | Course form data carrier |
| `DepartmentDTO.java` | DTO | Department form data carrier |
| `RegisterDTO.java` | DTO | Registration form data with @NotBlank validation |
| `Student.java` | Entity | Maps to `students` table (ManyToMany with Course) |
| `Teacher.java` | Entity | Maps to `teachers` table (ManyToOne Department, ManyToMany Student) |
| `Course.java` | Entity | Maps to `courses` table (ManyToOne Department) |
| `Department.java` | Entity | Maps to `departments` table (OneToMany Teacher & Course) |
| `User.java` | Entity | Maps to `users` table (login credentials + role) |
| `Role.java` | Entity | Enum: `STUDENT`, `TEACHER` |
| `StudentRepository.java` | Repository | JPA data access for students |
| `TeacherRepository.java` | Repository | JPA data access for teachers |
| `CourseRepository.java` | Repository | JPA data access for courses |
| `DepartmentRepository.java` | Repository | JPA data access for departments |
| `UserRepository.java` | Repository | JPA data access + custom queries (`findByUsername`) |
| `StudentService.java` | Service | Student business logic + role-based edit control |
| `TeacherService.java` | Service | Teacher business logic + student/department assignment |
| `CourseService.java` | Service | Course business logic + student enrollment |
| `DepartmentService.java` | Service | Department CRUD business logic |
| `UserService.java` | Service | Registration + BCrypt password encoding |
| `CustomUserDetailsService.java` | Security | Loads user from DB for Spring Security authentication |
| `CustomUserDetails.java` | Security | Wraps User entity → Spring's `UserDetails` interface |

---

## Viva One-Liners

| Topic | One-Liner Answer |
|---|---|
| **Spring Boot** | Framework that auto-configures Spring applications with embedded servers and opinionated defaults |
| **IoC Container** | Spring core that manages object lifecycle and dependency injection automatically |
| **Bean** | Spring-managed object created and injected by the IoC container |
| **Dependency Injection** | Design pattern where Spring provides required objects instead of creating them manually |
| **Constructor Injection** | Best practice DI — dependencies passed through constructor, ensures immutability |
| **MVC** | Pattern separating app into Model (data), View (UI), Controller (logic) |
| **JPA** | Java specification for ORM that maps Java objects to database tables using annotations |
| **Hibernate** | JPA implementation that generates SQL queries and manages database operations |
| **Entity** | Java class annotated with `@Entity` that maps directly to a database table |
| **DTO** | Simple object that carries data between layers without exposing the entity |
| **ModelMapper** | Library that automatically maps fields between Entity and DTO objects |
| **Repository** | Interface extending `JpaRepository` that provides CRUD without writing SQL |
| **Service** | Business logic layer between Controller and Repository, annotated with `@Service` |
| **Controller** | Handles HTTP requests, calls services, and returns views/responses |
| **Thymeleaf** | Server-side template engine that generates HTML from Java data |
| **Spring Security** | Framework handling authentication (who you are) and authorization (what you can do) |
| **BCrypt** | One-way password hashing algorithm that securely stores passwords |
| **Authentication** | Verifying user identity using credentials (username + password) |
| **Authorization** | Checking if authenticated user has permission to access a resource |
| **SecurityFilterChain** | Chain of filters intercepting every HTTP request to enforce security rules |
| **DaoAuthenticationProvider** | Authenticates by loading user from DB and comparing hashed passwords |
| **UserDetails** | Spring Security interface wrapping user info (username, password, roles) |
| **`@Transactional`** | Ensures DB operations in a method are atomic (all succeed or all rollback) |
| **Unit Test** | Tests individual methods in isolation using mock objects instead of real dependencies |
| **Mockito** | Framework that creates fake (mock) objects to isolate the unit being tested |
| **`@Mock`** | Creates a fake object — method calls return default values unless configured |
| **`@InjectMocks`** | Creates real object and injects `@Mock` objects into it |
| **`when().thenReturn()`** | Configures mock behavior — "when this is called, return that" |
| **`verify()`** | Checks that a mock method was called the expected number of times |
| **Docker** | Platform that packages apps into containers for consistent deployment everywhere |
| **Docker Compose** | Tool defining and running multi-container apps using YAML configuration |
| **Dockerfile** | Script with instructions to build a Docker image layer by layer |
| **Container** | Lightweight, isolated running instance of a Docker image |
| **Volume** | Persistent Docker storage that survives container restart/deletion |
| **CI/CD** | Practice of automatically building, testing, and deploying code on every change |
| **GitHub Actions** | GitHub's CI/CD that runs workflows (build + test) on push/PR events |
| **Branch Protection** | Rules preventing direct pushes to main — requires PR, review, and CI checks |
| **pom.xml** | Maven config defining dependencies, plugins, Java version, and build settings |
| **application.yml** | Spring Boot config for database, JPA, server, and Docker settings |
| **Lombok** | Library generating boilerplate (getters, setters, constructors) at compile time |
| **Maven Wrapper (`mvnw`)** | Script that downloads and uses correct Maven version — no install needed |
| **`ddl-auto: update`** | Hibernate updates DB schema on startup without losing existing data |
| **`@Valid`** | Triggers Jakarta Bean Validation on DTO fields (`@NotBlank`, `@Email`, etc.) |
| **`@PathVariable`** | Extracts value from URL path: `/students/{id}` → `id` variable |
| **`@ModelAttribute`** | Binds HTML form data to a Java DTO object automatically |

---

## Quick Reference Commands

```bash
# ===== BUILD =====
./mvnw clean package -DskipTests    # Build JAR (skip tests for speed)
./mvnw clean package                # Build JAR + run tests
./mvnw clean verify                 # Build + test + verify

# ===== TEST =====
./mvnw test                         # Run all tests
./mvnw test -Dtest=StudentServiceTest          # Specific test class
./mvnw test -Dtest=StudentServiceTest#method   # Specific test method

# ===== RUN =====
./mvnw spring-boot:run              # Run locally (needs PostgreSQL)

# ===== DOCKER =====
docker-compose up --build            # Build images & start services
docker-compose up -d                 # Start in background
docker-compose down                  # Stop & remove containers
docker-compose logs -f app           # View app logs live
docker ps                            # List running containers

# ===== GIT =====
git checkout -b feature/xyz          # Create feature branch
git add . && git commit -m "msg"     # Stage & commit changes
git push origin feature/xyz          # Push branch to GitHub
```

---

## Tech Stack Summary

| Technology | Version | Purpose |
|---|---|---|
| **Java** | 17 | Programming language |
| **Spring Boot** | 4.0.1 | Application framework |
| **Spring Security** | 7.0.2 | Authentication & authorization |
| **Spring Data JPA** | — | Database access (ORM) |
| **Hibernate** | — | JPA implementation |
| **Thymeleaf** | — | HTML template engine |
| **PostgreSQL** | 16 | Relational database |
| **Lombok** | 1.18.42 | Boilerplate code generation |
| **ModelMapper** | 3.2.4 | Entity ↔ DTO conversion |
| **Docker** | — | Containerization |
| **Docker Compose** | — | Multi-container orchestration |
| **JUnit 5** | — | Testing framework |
| **Mockito** | — | Mocking framework |
| **GitHub Actions** | — | CI/CD pipeline |
| **Maven** | — | Build tool & dependency management |

---

> **61 unit tests passing ✅ | Docker containerized 🐳 | GitHub Actions CI automated ⚙️**
