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
- [Unit Testing — Complete Deep Dive](#unit-testing--complete-deep-dive)
- [Docker & Docker Compose](#docker--docker-compose)
- [CI/CD Pipeline — GitHub Actions (Line-by-Line)](#cicd-pipeline--github-actions-line-by-line-deep-dive)
- [Branch Protection & GitHub Rulesets](#branch-protection--github-rulesets)
- [Complete Pipeline: Code → Test → CI → Merge](#complete-pipeline-code--test--ci--merge)
- [File-by-File Explanation](#file-by-file-explanation)
- [Viva One-Liners](#viva-one-liners)
- [Quick Reference Commands](#quick-reference-commands)

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

## Unit Testing — Complete Deep Dive

### What is Unit Testing?
Testing individual units (methods/classes) **in isolation** without external dependencies (database, network).

**Banglish:** Unit testing mane ekta specific method ba class k alada bhabe test kora — real database ba network lagbe na. Mock object diye dependency fake kore dewa hoy. Amader project e shob service class ke individually test kora hoyeche — DepartmentService, StudentService, etc. Real database er bodole Mockito diye fake (mock) repository use kora hoyeche.

### Unit Test vs Integration Test
| | Unit Test | Integration Test |
|---|---|---|
| **What** | Tests ONE method/class in isolation | Tests multiple components working together |
| **Database** | No real DB — uses Mock objects | Uses real/in-memory DB (H2) |
| **Speed** | Very fast (milliseconds) | Slower (seconds — loads Spring context) |
| **Example** | `DepartmentServiceTest` | `WebappApplicationTests` |
| **Annotation** | `@ExtendWith(MockitoExtension.class)` | `@SpringBootTest` |
| **Count in project** | 61 unit tests | 1 integration test |

**Banglish:** Unit test e amra single ekta method alada kore test kori — kono database lage na, mock diye fake kori. Integration test e full application load hoy ar real database (H2) connect kore — poortake ekshaathe kaj korche kina check kore.

---

### Step 1: Test Dependencies in pom.xml (What Makes Testing Possible)

Test dependencies declare kora hoyeche `pom.xml` file e. `<scope>test</scope>` mane ei dependency shudhu test cholbar somoy use hobe, production JAR e jabe na.

```xml
<!-- ===================== TEST DEPENDENCIES ===================== -->

<!-- JUnit 5 + Spring Test support (MockMvc, @SpringBootTest, etc.) -->
<!-- spring-boot-starter-test includes: JUnit 5, Mockito, AssertJ, Hamcrest, Spring Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>   <!-- Only available during 'mvn test', NOT in production JAR -->
</dependency>

<!-- Mockito-JUnit5 integration — enables @Mock, @InjectMocks, @ExtendWith -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 in-memory database — used by integration test instead of real PostgreSQL -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

| Dependency | What It Provides |
|---|---|
| `spring-boot-starter-*-test` | JUnit 5 + Spring Test + Mockito + AssertJ bundled together |
| `mockito-junit-jupiter` | `@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)` |
| `h2` | In-memory database for integration tests (no Docker needed) |

**Question:** `<scope>test</scope>` mane ki?
**Answer:** Ei dependency **shudhu test phase e available** — production build e include hoy na. `mvn test` cholale load hoy, kintu final `.jar` file e thake na.

---

### Step 2: Test Configuration — application-test.yml (Line by Line)

Integration test er jonno H2 in-memory database use kora hoy — real PostgreSQL Docker container er dorkar nei.

```yaml
# File: src/test/resources/application-test.yml
# PURPOSE: Override main application.yml settings for test environment

# Line 1: Spring configuration namespace
spring:

  # Lines 2-5: Database connection for tests
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    #     ↑ jdbc:h2:mem: = H2 in-memory database (RAM e thake, disk e save hoy na)
    #     ↑ testdb = database name (arbitrary)
    #     ↑ DB_CLOSE_DELAY=-1 = JVM off na hole database delete hobe na
    driver-class-name: org.h2.Driver    # H2 database driver (PostgreSQL driver er bodole)
    username: sa                         # Default H2 username
    password:                            # Empty password (H2 default)

  # Lines 6-8: JPA/Hibernate settings for tests
  jpa:
    hibernate:
      ddl-auto: create-drop
      # ↑ create-drop = Start e table create kore, shutdown e drop kore
      # ↑ Test er jonno perfect — fresh database every time, kono leftover data thake na

  # Lines 9-11: Disable data.sql for tests
  sql:
    init:
      mode: never
      # ↑ data.sql file SKIP koro
      # ↑ WHY? data.sql e PostgreSQL-specific syntax ache (ON CONFLICT)
      # ↑ H2 ei syntax bujhbe na → error dibe → tai disable kora hoyeche

  # Lines 12-14: Disable Docker Compose in tests
  docker:
    compose:
      enabled: false
      # ↑ Test cholbar somoy Docker container start korar dorkar nei
      # ↑ H2 in-memory database use korchi, PostgreSQL container lagbe na
```

| Setting | Production (`application.yml`) | Test (`application-test.yml`) |
|---|---|---|
| Database | PostgreSQL (Docker) | H2 in-memory (RAM) |
| `ddl-auto` | `update` (keep data) | `create-drop` (fresh each time) |
| `data.sql` | Runs (seeds data) | Disabled (`never`) |
| Docker Compose | Enabled | Disabled |

**Viva Question:** H2 keno use korlen test e, PostgreSQL keno na?
**Answer:** H2 in-memory database — RAM e cholae, kono install ba Docker lagena, test fast hoy, ar CI/CD pipeline e PostgreSQL container setup korar complexity avoid hoy. Test er jonno same SQL behavior dorkar — H2 seta provide kore.

---

### Step 3: Testing Stack & Framework

| Tool | Purpose | Version |
|---|---|---|
| **JUnit 5** (Jupiter) | Test framework — provides `@Test`, assertions, lifecycle | Bundled with Spring Boot |
| **Mockito** | Mocking framework — creates fake objects for isolation | Bundled with Spring Boot |
| **AssertJ** | Fluent assertion library (alternative to JUnit assertions) | Bundled with Spring Boot |
| **H2 Database** | In-memory SQL database for integration tests | Bundled via pom.xml |

### JUnit 5 Architecture
```
JUnit Platform      → Foundation for launching test frameworks (test engine API)
    ↓
JUnit Jupiter       → JUnit 5 annotations & assertions (@Test, assertEquals)
    ↓
JUnit Vintage       → Backward compatibility with JUnit 3/4 (not used here)
```

---

### Step 4: Test File Structure & Naming Convention

```
src/test/java/com/example/webapp/
├── WebappApplicationTests.java          ← Integration test (loads full Spring context)
├── service/
│   ├── DepartmentServiceTest.java       ← Unit tests for DepartmentService
│   ├── StudentServiceTest.java          ← Unit tests for StudentService  
│   ├── TeacherServiceTest.java          ← Unit tests for TeacherService
│   ├── CourseServiceTest.java           ← Unit tests for CourseService
│   └── UserServiceTest.java            ← Unit tests for UserService
src/test/resources/
└── application-test.yml                 ← Test profile config (H2 database)
```

**Naming Convention:**
- Test class: `{ClassName}Test.java` → `DepartmentServiceTest.java`
- Test method: `{methodName}_{scenario}_{expectedResult}` → `getDepartmentById_WhenNotFound_ShouldReturnEmpty()`
- Maven Surefire plugin auto-detects files ending with `Test.java` or `Tests.java`

---

### Step 5: Anatomy of a Unit Test Class (Line by Line)

```java
// ===== File: DepartmentServiceTest.java =====

package com.example.webapp.service;               // Same package as the class being tested

// ===== IMPORTS: Test framework + Mock framework + Assertions =====
import org.junit.jupiter.api.BeforeEach;           // Setup method annotation
import org.junit.jupiter.api.Test;                  // Marks a method as test
import org.junit.jupiter.api.extension.ExtendWith;  // Registers extensions (Mockito)
import org.mockito.InjectMocks;                     // Auto-inject mocks into target
import org.mockito.Mock;                            // Create fake object
import org.mockito.junit.jupiter.MockitoExtension;  // Mockito-JUnit5 bridge

import static org.junit.jupiter.api.Assertions.*;   // assertEquals, assertNotNull, etc.
import static org.mockito.Mockito.*;                 // when(), verify(), times()

// ===== CLASS-LEVEL ANNOTATION =====
@ExtendWith(MockitoExtension.class)
// ↑ WHAT: Tells JUnit 5 to use Mockito extension
// ↑ WHY: Without this, @Mock and @InjectMocks won't work
// ↑ HOW: MockitoExtension initializes all @Mock fields before each test
class DepartmentServiceTest {

    // ===== MOCK OBJECTS (FAKE dependencies) =====
    @Mock
    private DepartmentRepository departmentRepository;
    // ↑ WHAT: Creates a FAKE DepartmentRepository
    // ↑ WHY: We don't want real database calls — we're testing service logic ONLY
    // ↑ HOW: All methods return default values (null, 0, empty) unless configured with when()

    @Mock
    private ModelMapper modelMapper;
    // ↑ WHAT: Creates fake ModelMapper (no real DTO↔Entity conversion)

    // ===== SYSTEM UNDER TEST (the REAL object being tested) =====
    @InjectMocks
    private DepartmentService departmentService;
    // ↑ WHAT: Creates a REAL DepartmentService instance
    // ↑ HOW: Mockito automatically passes @Mock objects into its constructor
    // ↑ RESULT: departmentService uses FAKE repository, so no DB calls happen

    // ===== TEST DATA (shared across tests) =====
    private Department department;
    private DepartmentDTO departmentDTO;

    // ===== SETUP METHOD (runs BEFORE each @Test method) =====
    @BeforeEach
    void setUp() {
        // ↑ WHAT: Creates fresh test data before EVERY test
        // ↑ WHY: Tests should be independent — one test shouldn't affect another
        department = new Department();
        department.setId(1L);
        department.setName("Computer Science");

        departmentDTO = new DepartmentDTO();
        departmentDTO.setId(1L);
        departmentDTO.setName("Computer Science");
    }

    // ===== INDIVIDUAL TEST METHODS =====
    @Test                                            // Marks as test method
    void getAllDepartments_ShouldReturnList() {       // Descriptive name
        // Arrange → Act → Assert pattern (AAA)
    }
}
```

### Execution Flow (What Happens When Tests Run)
```
1. JUnit 5 discovers DepartmentServiceTest class
2. MockitoExtension activates (creates @Mock objects)
3. For EACH @Test method:
   a. @BeforeEach setUp() runs → creates fresh test data
   b. @InjectMocks creates real DepartmentService with mock dependencies
   c. Test method executes (Arrange → Act → Assert)
   d. Test passes ✅ or fails ❌
4. Report generated: X tests passed, Y failed
```

---

### Step 6: Test Pattern — Arrange-Act-Assert (AAA) Deep Dive

Every test follows the **AAA pattern** — the 3 phases of a unit test:

```java
@Test
void getAllDepartments_ShouldReturnListOfDepartments() {

    // ====== ARRANGE (Setup) ======
    // Configure mock behavior: "when findAll() is called, return this fake list"
    List<Department> departments = Arrays.asList(department);
    when(departmentRepository.findAll()).thenReturn(departments);
    // ↑ when(): Tells mock WHAT to do when a specific method is called
    // ↑ thenReturn(): The fake return value
    // ↑ NO real database query happens — just returns our fake list

    // ====== ACT (Execute) ======
    // Call the REAL service method being tested
    List<Department> result = departmentService.getAllDepartments();
    // ↑ This calls the REAL DepartmentService.getAllDepartments()
    // ↑ Inside, it calls departmentRepository.findAll()
    // ↑ But repository is a MOCK → returns our fake list from Arrange step

    // ====== ASSERT (Verify) ======
    // Check: Did we get the expected result?
    assertNotNull(result);                                    // Not null?
    assertEquals(1, result.size());                           // Exactly 1 item?
    assertEquals("Computer Science", result.get(0).getName()); // Name matches?

    // Check: Was the mock called correctly?
    verify(departmentRepository, times(1)).findAll();
    // ↑ verify(): Did findAll() get called exactly 1 time?
    // ↑ If service called findAll() 0 times or 2 times → test FAILS
}
```

**Banglish:** Arrange e amra mock setup kori — "jakhon findAll() call hobe takhon ei fake list return koro." Act e real service method call kori. Assert e check kori result thik ache kina ar mock method correctly call hoyeche kina.

### Testing Exception Scenarios
```java
@Test
void updateDepartment_WhenNotFound_ShouldThrowException() {
    // Arrange: Mock returns empty — department doesn't exist
    when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert: Expect RuntimeException to be thrown
    assertThrows(RuntimeException.class, () -> {
        departmentService.updateDepartment(99L, departmentDTO);
    });
    // ↑ assertThrows: "ei code cholale RuntimeException ashbe"
    // ↑ If no exception thrown → test FAILS
    // ↑ If wrong exception type → test FAILS
}
```

### Testing Void Methods (Delete)
```java
@Test
void deleteDepartment_ShouldCallRepositoryDelete() {
    // Arrange: delete returns void, so use doNothing()
    doNothing().when(departmentRepository).deleteById(1L);
    // ↑ doNothing(): Since deleteById() returns void, just do nothing

    // Act
    departmentService.deleteDepartment(1L);

    // Assert: Verify the mock method was called
    verify(departmentRepository, times(1)).deleteById(1L);
    // ↑ We can't check return value (void), so we verify the CALL happened
}
```

---

### Step 7: Mockito Methods — Complete Reference

| Method | What It Does | Example |
|---|---|---|
| `when(X).thenReturn(Y)` | "When X is called, return Y" | `when(repo.findAll()).thenReturn(list)` |
| `when(X).thenThrow(E)` | "When X is called, throw exception E" | `when(repo.findById(99L)).thenThrow(new RuntimeException())` |
| `doNothing().when(X).method()` | For void methods — do nothing | `doNothing().when(repo).deleteById(1L)` |
| `verify(mock, times(n)).method()` | Check method was called n times | `verify(repo, times(1)).save(any())` |
| `verify(mock, never()).method()` | Check method was NEVER called | `verify(repo, never()).deleteById(any())` |
| `any(Class.class)` | Matches any argument of that type | `when(repo.save(any(Student.class)))` |
| `eq(value)` | Matches exact argument value | `verify(repo).findById(eq(1L))` |
| `assertNotNull(X)` | X is not null | `assertNotNull(result)` |
| `assertEquals(A, B)` | A equals B | `assertEquals("CS", dept.getName())` |
| `assertTrue(X)` | X is true | `assertTrue(result.isPresent())` |
| `assertFalse(X)` | X is false | `assertFalse(result.isPresent())` |
| `assertThrows(E, code)` | Code throws exception E | `assertThrows(RuntimeException.class, () -> ...)` |

---

### Step 8: Integration Test — WebappApplicationTests (Line by Line)

```java
// ===== File: WebappApplicationTests.java =====

@SpringBootTest
// ↑ WHAT: Loads the FULL Spring application context (all beans, configs, security)
// ↑ WHY: Verifies the entire app starts correctly — no configuration errors
// ↑ HOW: Creates real Spring IoC container with all @Service, @Repository, @Controller beans
// ↑ DIFFERENCE from @ExtendWith(MockitoExtension.class): This loads EVERYTHING, not just mocks

@ActiveProfiles("test")
// ↑ WHAT: Activates "test" profile → loads application-test.yml
// ↑ WHY: Uses H2 in-memory DB instead of real PostgreSQL
// ↑ HOW: Spring looks for src/test/resources/application-test.yml and overrides main config

class WebappApplicationTests {

    @Test
    void contextLoads() {
        // ↑ WHAT: Simplest integration test — just verifies Spring context loads
        // ↑ HOW: If ANY bean fails to create (wrong config, missing dependency)
        //        → this test FAILS with a detailed error
        // ↑ Empty body is intentional — loading the context IS the test
        // ↑ If this passes → all @Configuration, @Service, @Repository, @Controller
        //   beans are correctly wired together
    }
}
```

**Viva Question:** `contextLoads()` method er body keno empty?
**Answer:** `@SpringBootTest` annotation Spring er full application context load kore. Jodi kono bean create korte problem hoy (configuration error, missing dependency, circular reference) tahole **context load er somoy e test fail hoye jabe**. Empty body mane — context successfully load howa TAI hocche test. Body te extra kichu likhte hoy na.

---

### Step 9: Test Coverage (62 Tests Total, 0 Skipped, All Passing ✅)

| Test Class | Tests | What's Tested |
|---|---|---|
| `DepartmentServiceTest` | 7 | getAllDepartments, getById (found/not-found), save, update (found/not-found), delete |
| `StudentServiceTest` | 14 | CRUD + course assignment + self-edit restriction + role checks |
| `TeacherServiceTest` | 14 | CRUD + student assignment + department linking |
| `CourseServiceTest` | 13 | CRUD + student enrollment + department linking |
| `UserServiceTest` | 13 | Registration + password encoding + duplicate username check |
| `WebappApplicationTests` | 1 | Spring context loads successfully (integration test) |
| **Total** | **62** | **All service methods + application context** |

---

### Step 10: Running Tests — Commands for Viva Demo

```bash
# ===== 1. Run ALL tests (62 tests) =====
./mvnw test
# ↑ Maven Surefire plugin finds all *Test.java files
# ↑ Runs them with JUnit 5 engine
# ↑ Output: Tests run: 62, Failures: 0, Errors: 0, Skipped: 0

# ===== 2. Run a SPECIFIC test class =====
./mvnw test -Dtest=DepartmentServiceTest
# ↑ -Dtest= flag specifies which test class to run
# ↑ Only runs 7 tests from DepartmentServiceTest

# ===== 3. Run a SPECIFIC test method =====
./mvnw test -Dtest=DepartmentServiceTest#getAllDepartments_ShouldReturnListOfDepartments
# ↑ ClassName#methodName format
# ↑ Runs only that ONE test method

# ===== 4. Run tests with verbose output =====
./mvnw test -Dsurefire.useFile=false
# ↑ Shows individual test names and PASS/FAIL in console

# ===== 5. Run tests and generate reports =====
./mvnw test
# ↑ Reports auto-generated at: target/surefire-reports/
# ↑ XML reports for CI tools, TXT reports for human reading

# ===== 6. Run verify (compile + test + verify) =====
./mvnw clean verify
# ↑ clean: delete previous build (target/ folder)
# ↑ verify: compile → test → verify (more thorough than just 'test')
# ↑ This is what CI pipeline runs

# ===== 7. Skip tests (for quick build only) =====
./mvnw clean package -DskipTests
# ↑ -DskipTests: skip ALL tests (useful for quick Docker image build)
# ↑ WARNING: Never use this in CI — tests must always run!
```

### Expected Output When Tests Pass ✅
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.webapp.WebappApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.webapp.service.CourseServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.webapp.service.DepartmentServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.webapp.service.StudentServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.webapp.service.TeacherServiceTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.example.webapp.service.UserServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO] Tests run: 62, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### What Happens When Tests FAIL ❌
```
[ERROR] Tests run: 62, Failures: 1, Errors: 0, Skipped: 0
[ERROR] DepartmentServiceTest.getAllDepartments_ShouldReturnList:45
         Expected: 1
         Actual:   0
[INFO] BUILD FAILURE    ← Build fails, CI pipeline marks PR as failed
```

---

### Unit Testing — Viva Q&A

| Question | Answer |
|---|---|
| **Unit test ki?** | Ekta single method/class ke individually test kora — isolation e, kono real dependency ney |
| **Mock ki?** | Fake object ja real dependency er bodole use hoy — `@Mock` diye toiri hoy |
| **`@InjectMocks` ki kore?** | Real object toiri kore ar shob `@Mock` objects oi object er constructor e inject kore dey |
| **`when().thenReturn()` ki?** | Mock ke bole — "jakhon ei method call hobe takhon ei value return koro" |
| **`verify()` keno lagbe?** | Assert kore je mock method thik thik call hoyeche — koibar call hoyeche seta o check kore |
| **AAA pattern ki?** | Arrange (setup) → Act (execute) → Assert (verify) — test likhbar structure |
| **Unit test e DB lage ki?** | Na! Mock diye fake kora hoy. Real DB diye integration test cholae |
| **`@BeforeEach` ki kore?** | PROTTEK `@Test` method er AGEE run kore — fresh test data toiri kore |
| **`@SpringBootTest` vs `@ExtendWith(MockitoExtension.class)` difference?** | `@SpringBootTest` full app load kore (slow, integration). `@ExtendWith` shudhu Mockito enable kore (fast, unit) |
| **Keno 62 tests, 0 skipped?** | 61 unit (5 service class) + 1 integration (contextLoads). Shob pass kore, kono skip nei |
| **Test class er naam convention ki?** | `{ClassName}Test.java` — Maven Surefire plugin auto-detect kore |
| **`assertThrows` keno use kori?** | Exception throw hocche kina check korar jonno — wrong input dile error ashbe seta verify kori |

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

## CI/CD Pipeline — GitHub Actions (Line-by-Line Deep Dive)

### What is CI/CD?
- **CI (Continuous Integration)**: Automatically build and test code on every push/PR — bugs ber kora hoy merge er AGEE
- **CD (Continuous Deployment/Delivery)**: Automatically deploy after tests pass (amader project e shudhu CI ache)

**Banglish:** CI mane protibar code push korle ba PR create korle GitHub AUTOMATICALLY tomar code compile kore ar 62 ta test run kore. Jodi kono test fail kore, PR te red cross (❌) dekha hoy — merge korte dibe na. Shob pass korle green tick (✅) dekha hoy — merge korte parbe.

### CI Pipeline Visual Flow
```
Developer pushes code to GitHub
    │
    ▼
GitHub detects push/PR event → matches on: trigger
    │
    ▼
GitHub allocates Ubuntu VM (Runner) → fresh machine every time
    │
    ▼
Step 1: actions/checkout@v4 → downloads your repo code
    │
    ▼
Step 2: actions/setup-java@v4 → installs JDK 17 + caches Maven
    │
    ▼
Step 3: chmod +x ./mvnw → makes Maven wrapper executable
    │
    ▼
Step 4: ./mvnw clean verify → compiles code + runs all 62 tests
    │                              │
    │                              ├── Tests PASS ✅ → PR gets green check
    │                              └── Tests FAIL ❌ → Step 5 runs
    │
    ▼
Step 5 (only on failure): Upload test reports as artifact for debugging
```

---

### GitHub Actions Concepts — Must Know for Viva

| Concept | What It Is | Analogy |
|---|---|---|
| **Workflow** | A YAML file defining automated tasks | Recipe book |
| **Event/Trigger** | What starts the workflow (push, PR, schedule) | Doorbell |
| **Job** | A group of steps running on same machine | Chef cooking a meal |
| **Step** | A single task inside a job | One cooking instruction |
| **Runner** | The machine (VM) that executes the job | Kitchen |
| **Action** | Pre-built reusable step (from GitHub Marketplace) | Ready-made ingredient |
| **Artifact** | Files saved from a workflow run (logs, reports) | Leftover ingredients |

**Banglish:**
- **Workflow** = `.yml` file ja bole KI korte hobe
- **Event** = KAKHON korte hobe (push hole, PR hole)
- **Job** = Ekta machine e ki ki step run hobe
- **Step** = Ekek ta command/action
- **Runner** = GitHub er free VM (Ubuntu) — tomar code oi machine e cholbe
- **Action** = Ready-made step (checkout, setup-java) — marketplace theke ney
- **Artifact** = Output file save kore rakha (test report, build log)

---

### ci.yml — Complete Line-by-Line Explanation

**File Location:** `.github/workflows/ci.yml`
**File Format:** YAML (Yet Another Markup Language) — indentation-based config format

```yaml
# ===================================================================
# LINE 1-3: COMMENTS (starts with #, GitHub ignores these)
# ===================================================================
# GitHub Actions CI/CD Pipeline
# WHAT: Automatically runs build + tests on every push/PR to main branch
# HOW: GitHub triggers this workflow, spins up an Ubuntu VM, installs JDK, runs Maven


# ===================================================================
# LINE 5: WORKFLOW NAME
# ===================================================================
name: CI - Build & Test
# ↑ KEY: name
# ↑ VALUE: "CI - Build & Test"
# ↑ WHAT: Display name shown in GitHub Actions tab and PR check status
# ↑ WHERE: Visible in GitHub → Actions tab, and in PR status checks
# ↑ IMPORTANT: This name is used in Branch Protection Rules as "status check"
#   → You set "CI - Build & Test / build-and-test" as required check


# ===================================================================
# LINE 7-11: EVENT TRIGGERS (WHEN does the workflow run?)
# ===================================================================
on:
# ↑ KEY: on (reserved keyword)
# ↑ WHAT: Defines WHICH GitHub events trigger this workflow
# ↑ Without 'on:', workflow will NEVER run

  push:
  # ↑ Event type 1: When someone pushes commits
    branches: [ main ]
    # ↑ FILTER: Only trigger when push is to 'main' branch
    # ↑ Push to 'feature/xyz' branch → WILL NOT trigger
    # ↑ Push to 'main' branch → WILL trigger

  pull_request:
  # ↑ Event type 2: When a Pull Request is created or updated
    branches: [ main ]
    # ↑ FILTER: Only trigger when PR targets 'main' branch
    # ↑ PR: feature/xyz → main → WILL trigger
    # ↑ PR: feature/xyz → develop → WILL NOT trigger
    # ↑ Every new commit pushed to the PR branch re-triggers the workflow


# ===================================================================
# LINE 13: JOBS SECTION (WHAT to do)
# ===================================================================
jobs:
# ↑ KEY: jobs (reserved keyword)
# ↑ WHAT: Container for all jobs in this workflow
# ↑ A workflow can have multiple jobs (build, test, deploy)
# ↑ Multiple jobs run in PARALLEL by default (unless needs: is specified)

  build-and-test:
  # ↑ JOB ID: "build-and-test" (custom name, used as identifier)
  # ↑ WHAT: Our only job — compiles code and runs all tests
  # ↑ This ID appears in GitHub as "CI - Build & Test / build-and-test"


# ===================================================================
# LINE 16-17: RUNNER (WHERE does the job run?)
# ===================================================================
    runs-on: ubuntu-latest
    # ↑ KEY: runs-on (specifies which machine to use)
    # ↑ VALUE: ubuntu-latest (GitHub's free Ubuntu Linux VM)
    # ↑ WHAT: GitHub creates a FRESH Ubuntu VM for this job
    # ↑ FRESH means: no leftover files, clean OS, fresh JDK install needed
    # ↑ OTHER OPTIONS: windows-latest, macos-latest (but Ubuntu is fastest & free)
    # ↑ WHY UBUNTU? Free for public repos, faster than Windows/Mac, our app is cross-platform


# ===================================================================
# LINE 19: STEPS (individual tasks within the job)
# ===================================================================
    steps:
    # ↑ KEY: steps (list of sequential tasks)
    # ↑ Steps run ONE BY ONE, top to bottom
    # ↑ If any step fails → subsequent steps are SKIPPED (unless if: failure())


# ===================================================================
# STEP 1: CHECKOUT CODE
# ===================================================================
      - name: Checkout code
      # ↑ name: Human-readable step name (shown in GitHub Actions log)
        uses: actions/checkout@v4
        # ↑ uses: Runs a pre-built GitHub Action (from Marketplace)
        # ↑ actions/checkout@v4: Official action that downloads repo code
        # ↑ @v4: Version 4 of the checkout action
        # ↑ WHAT IT DOES:
        #   1. Clones your repo into the runner VM
        #   2. Checks out the correct branch/PR commit
        #   3. Sets up .git directory for git commands
        # ↑ WITHOUT THIS: The VM has NO code — every other step would fail


# ===================================================================
# STEP 2: SETUP JAVA JDK 17
# ===================================================================
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        # ↑ Official action that installs Java JDK on the runner
        with:
          java-version: '17'
          # ↑ Install JDK 17 (matches our pom.xml <java.version>17</java.version>)
          # ↑ MUST match project's Java version — otherwise compilation errors

          distribution: 'temurin'
          # ↑ Use Eclipse Temurin JDK distribution (same as our Dockerfile: eclipse-temurin:17-jdk)
          # ↑ OPTIONS: temurin, corretto (Amazon), zulu (Azul), adopt
          # ↑ Temurin = most popular open-source JDK distribution

          cache: maven
          # ↑ CACHE Maven dependencies (~/.m2/repository folder)
          # ↑ WHAT: First run downloads all dependencies (slow ~2min)
          #         Next runs use cached dependencies (fast ~10sec)
          # ↑ HOW: Creates a hash of pom.xml → if pom.xml unchanged, use cache
          # ↑ WHY: Without cache, every CI run downloads 200+ MB of dependencies
          # ↑ HUGE time saver: 3-4 min → 30 sec


# ===================================================================
# STEP 3: MAKE MAVEN WRAPPER EXECUTABLE
# ===================================================================
      - name: Make Maven wrapper executable
        run: chmod +x ./mvnw
        # ↑ run: Executes a shell command (bash on Ubuntu)
        # ↑ chmod +x: Changes file permission to "executable"
        # ↑ ./mvnw: Maven Wrapper script
        # ↑ WHY NEEDED?
        #   On Windows (your PC): mvnw.cmd is used, permissions don't matter
        #   On Linux (CI runner): ./mvnw needs execute permission (chmod +x)
        #   Git on Windows stores files as 100644 (non-executable)
        #   Linux requires 100755 (executable) to run scripts
        # ↑ WITHOUT THIS: "Permission denied" error → CI fails in 6 seconds


# ===================================================================
# STEP 4: BUILD AND TEST (THE MAIN STEP)
# ===================================================================
      - name: Build and Test
        run: ./mvnw clean verify
        # ↑ run: Executes Maven command
        # ↑ ./mvnw: Maven Wrapper (uses project's Maven version, not system's)
        # ↑ clean: Delete target/ folder (fresh build, no stale files)
        # ↑ verify: Run the full Maven lifecycle:
        #
        #   Maven Lifecycle Phases (in order):
        #   1. validate    → Check pom.xml is correct
        #   2. compile     → Compile Java source code → .class files
        #   3. test        → Run unit tests (JUnit 5 + Mockito) ← ALL 62 TESTS RUN HERE
        #   4. package     → Create JAR file (target/webapp-0.0.1-SNAPSHOT.jar)
        #   5. verify      → Run integration checks
        #
        # ↑ If ANY test fails → Maven exits with error code → Step fails → Job fails
        # ↑ Test profile automatically used: H2 in-memory DB, no Docker needed
        #
        # WHY 'verify' not just 'test'?
        #   'verify' includes everything 'test' does PLUS additional checks
        #   More thorough validation for CI


# ===================================================================
# STEP 5: UPLOAD TEST REPORTS (ONLY ON FAILURE)
# ===================================================================
      - name: Upload Test Reports
        if: failure()
        # ↑ if: failure() → This step runs ONLY if a previous step failed
        # ↑ Normal behavior: if step 4 passes, this step is SKIPPED
        # ↑ On failure: This step runs to save debug information

        uses: actions/upload-artifact@v4
        # ↑ Official action that saves files from the CI run

        with:
          name: test-reports
          # ↑ Artifact name (like a zip file label)
          # ↑ Downloadable from GitHub → Actions → Workflow run → Artifacts

          path: target/surefire-reports/
          # ↑ WHERE test reports are generated by Maven Surefire plugin
          # ↑ Contains: XML reports + TXT reports for each test class
          # ↑ These files tell you EXACTLY which test failed and why
```

---

### YAML Syntax Guide (For Viva)

```yaml
# KEY-VALUE pair
name: CI - Build & Test          # String value (quotes optional for simple strings)

# NESTED keys (indentation = hierarchy, SPACES only, NO TABS)
on:
  push:                          # push is child of on:
    branches: [ main ]           # Array shorthand (same as multiline with -)

# List/Array (begins with -)
steps:
  - name: Step 1                 # Each - starts a new list item
    run: echo hello
  - name: Step 2
    uses: actions/checkout@v4

# Dictionary inside list
with:
  java-version: '17'            # key: value pair inside 'with' block
  distribution: 'temurin'

# Conditional execution
if: failure()                    # Built-in GitHub Actions function
```

| YAML Rule | Example | Wrong |
|---|---|---|
| Spaces for indentation (2 spaces) | `  steps:` | `\tsteps:` (tabs) |
| Key-value with colon+space | `name: CI` | `name:CI` (no space) |
| Array with brackets | `branches: [ main ]` | `branches: main` |
| String with special chars | `java-version: '17'` | `java-version: 17` (works but '17' is safer) |
| Comments with # | `# This is a comment` | `// comment` (wrong syntax) |

---

### Maven Lifecycle Phases (What `./mvnw clean verify` Does)

```
clean           → Deletes target/ directory (removes old build artifacts)
  │
  ▼
validate        → Checks pom.xml is valid
  │
  ▼
compile         → Compiles src/main/java → target/classes/
  │                Lombok generates getters/setters at this phase
  ▼
test-compile    → Compiles src/test/java → target/test-classes/
  │
  ▼
test            → Runs all *Test.java files using Surefire plugin
  │                JUnit 5 engine executes @Test methods
  │                Mockito creates @Mock objects
  │                62 tests run (61 unit + 1 integration)
  │                Reports: target/surefire-reports/
  ▼
package         → Creates JAR: target/webapp-0.0.1-SNAPSHOT.jar
  │
  ▼
verify          → Runs integration verifications
                   ALL DONE ✅ — or FAILURE ❌ if any step failed
```

**Viva Question:** `mvnw test` vs `mvnw verify` — difference ki?
**Answer:** `test` shudhu test phase porjonto chole (compile → test). `verify` aro aage jay — package koreo plus additional verification checks cholae. CI te `verify` use kori karon more thorough.

---

### How GitHub Actions Runs Our Pipeline (Behind the Scenes)

```
1. Developer pushes code → git push origin feature/xyz

2. GitHub receives push → checks .github/workflows/*.yml files

3. Event matching:
   - Push to main? → YES: trigger    / NO: skip
   - PR targeting main? → YES: trigger / NO: skip

4. GitHub allocates a Runner (Ubuntu VM):
   - Fresh VM (2 CPU, 7 GB RAM, 14 GB SSD)
   - OS: Ubuntu 22.04 LTS
   - Pre-installed: git, curl, docker (but NOT Java)

5. Steps execute sequentially:
   Step 1: git clone https://github.com/user/repo → code downloaded
   Step 2: Download + install JDK 17, cache ~/.m2 directory
   Step 3: chmod +x ./mvnw → make Maven wrapper executable
   Step 4: ./mvnw clean verify → compile + run 62 tests
   Step 5: (only if Step 4 fails) upload test reports

6. Result reported back to GitHub:
   - Job passes ✅ → Green check on PR / commit
   - Job fails ❌ → Red cross on PR / commit → merge blocked (if rules set)

7. VM is DESTROYED after workflow completes (no data persists)
```

---

### CI/CD — Viva Q&A

| Question | Answer |
|---|---|
| **CI ki?** | Continuous Integration — protibar push/PR e automatically code build + test kora |
| **CD ki?** | Continuous Deployment/Delivery — test pass hole automatically deploy kora (amader project e nei) |
| **GitHub Actions ki?** | GitHub-er built-in CI/CD system — YAML file likhe workflow define koro |
| **Workflow ki?** | `.yml` file ja define kore KAKHON ar KI korte hobe |
| **Runner ki?** | GitHub-er free Ubuntu VM ja tomar code execute kore — every run e fresh machine |
| **`runs-on: ubuntu-latest` keno?** | Free, fast, ar amader Java app cross-platform tai Linux e cholbe |
| **`actions/checkout@v4` ki kore?** | Tomar GitHub repo runner VM e clone/download kore |
| **`cache: maven` keno dorkar?** | Dependencies (~200MB) bar bar download kora slow — cache saves 3-4 minutes |
| **`chmod +x ./mvnw` keno laglo?** | Windows e file permission 100644 (non-executable). Linux e 100755 lagbe — noyto "Permission denied" error |
| **`clean verify` vs `test` difference?** | `verify` = compile + test + package + verify. `test` shudhu compile + test |
| **`if: failure()` ki?** | Ei step shudhu TOKHON i cholbe jakhon previous kono step fail koreche |
| **Artifact ki?** | CI run theke file save kora (test report) — pore download kore debug korte paro |
| **CI fail hole ki hoy?** | PR te red ❌ cross dekhay, merge button disabled hoy (branch protection thakle) |
| **CI koto somoy lage?** | First run: ~3-4 min (downloads dependencies). Cached runs: ~1-2 min |
| **Runner e ki Java installed thake?** | Na! `actions/setup-java@v4` diye install korte hoy. Runner shudhu OS + git diye ashe |
| **YAML e tab use korle ki hobe?** | ERROR! YAML shudhu spaces allow kore. Tab use korle syntax error hobe |

---

## Branch Protection & GitHub Rulesets

### What are Branch Protection Rules?
Rules on GitHub that prevent direct pushes to important branches (like `main`), ensuring code goes through Pull Request, CI checks, and review before merging.

**Banglish:** Branch protection mane rules set kora — keu `main` branch-e directly push korte parbe na. First feature branch-e code likhte hobe, PR create korte hobe, CI (62 tests) pass korte hobe, tarpor merge hobe. Etar fole `main` branch SHOB somoy safe thake.

### GitHub Rulesets (New UI — replaces classic Branch Protection)

**Navigation:** Repository → Settings → Rules → Rulesets → New ruleset → New branch ruleset

#### Ruleset Configuration:
| Setting | Value | Why |
|---|---|---|
| **Ruleset Name** | `protect-main` | Descriptive name |
| **Enforcement status** | **Active** | Must be Active to enforce! Disabled = rules don't apply |
| **Bypass list** | *Empty* | Nobody can bypass — rules apply to everyone, including admins |
| **Target branches** | `main` | Rules apply to the `main` branch |

#### Rules Enabled:
| Rule | What It Does | Why Important |
|---|---|---|
| ✅ **Restrict deletions** | Nobody can delete `main` branch | Production branch must never be deleted |
| ✅ **Require a pull request before merging** | Cannot push directly to `main` | All changes must go through PR for visibility |
| ✅ **Require status checks to pass** | CI (Build & Test) must pass before merge | Broken code cannot enter `main` |
| ✅ **Block force pushes** | Cannot `git push --force` to `main` | Prevents history rewriting/data loss |
| ✅ **Require conversation resolution** | All review comments must be resolved | Ensures feedback is addressed before merge |

#### Required Status Check:
- Check name: **`build-and-test`** (matches the `jobs:` key in ci.yml)
- Source: **GitHub Actions**

### How Status Check Works
```
1. Developer creates PR: feature/xyz → main
2. GitHub sees PR targets main → triggers ci.yml workflow
3. CI runs: checkout → setup JDK → chmod +x → ./mvnw clean verify (62 tests)
4. CI result sent back to PR:
   ├── PASS ✅ → "build-and-test" check passes → Merge button ENABLED
   └── FAIL ❌ → "build-and-test" check fails → Merge button DISABLED
5. Developer must fix code, push again → CI re-runs → until it passes
```

### Protected Branch Workflow — Complete Git Commands

```bash
# ===== STEP 1: Make sure you're on main and up to date =====
git checkout main
git pull origin main

# ===== STEP 2: Create a NEW feature branch =====
git checkout -b feature/add-enrollment
# ↑ -b: Create AND switch to new branch
# ↑ Branch name convention: feature/description, fix/description, hotfix/description

# ===== STEP 3: Make your code changes =====
# (edit files, add features, fix bugs)

# ===== STEP 4: Stage and commit changes =====
git add .
# ↑ Stages ALL changed files

git commit -m "Add student enrollment to courses"
# ↑ Commits with descriptive message

# ===== STEP 5: Push feature branch to GitHub =====
git push origin feature/add-enrollment
# ↑ Pushes your branch to GitHub remote
# ↑ First push of new branch → GitHub shows "Create Pull Request" link

# ===== STEP 6: Create Pull Request on GitHub =====
# Go to GitHub → Your repo → "Compare & pull request" button
# Base: main ← Compare: feature/add-enrollment
# Add title and description → Create Pull Request

# ===== STEP 7: Wait for CI to pass =====
# GitHub Actions runs automatically → 62 tests execute
# Watch: GitHub → Pull Request → Checks tab

# ===== STEP 8: Merge after CI passes =====
# Merge button becomes green → Click "Merge pull request"
# Delete feature branch (cleanup)

# ===== STEP 9: Update your local main =====
git checkout main
git pull origin main
# ↑ Now your local main has the merged changes
```

### What If You Try to Push Directly to main?
```bash
git checkout main
git push origin main
# ↑ ERROR! GitHub rejects the push:
# remote: error: GH006: Protected branch update failed
# remote: error: Required status check "build-and-test" is expected
# ↑ SOLUTION: Create a feature branch and PR instead
```

### Branch Protection — Viva Q&A

| Question | Answer |
|---|---|
| **Branch protection keno lagbe?** | `main` branch safe rakhte — keu directly broken code push korte parbe na |
| **Ruleset vs classic branch protection?** | Ruleset = GitHub er new UI, more flexible. Classic = purano system. Same concept. |
| **Bypass list empty keno?** | Keu bypass korte parbe na — even admin ke o PR er modhye diye jete hobe |
| **Status check ki?** | CI pipeline er result — "build-and-test" job pass korte HOBE merge korar agee |
| **Force push keno block?** | `git push --force` history rewrite kore — aager commits moche jay, dangerous |
| **Direct push korle ki hobe?** | GitHub reject kore — error message dekhay, push hoy na |
| **CI fail hole merge korte parbo?** | Na! Merge button disabled thake jokhon tak na CI pass kore |
| **Branch protection ki locally check kore?** | Na! Shudhu GitHub server e check hoy. Local e commit/push korte parbe, kintu GitHub reject korbe |

---

## Complete Pipeline: Code → Test → CI → Merge

This section traces the ENTIRE journey from writing code to merging — step by step, exactly as you would demo in a viva.

### Phase 1: Write Code Locally
```bash
# 1. Open project in VS Code / IntelliJ
# 2. Make sure you're on a feature branch
git checkout -b feature/add-enrollment

# 3. Write your code changes (edit Java files, add features)
# 4. Write/update unit tests for changed code
```

### Phase 2: Run Tests Locally (Before Pushing)
```bash
# Run all 62 tests locally
./mvnw test

# Expected output:
# Tests run: 62, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS

# If tests fail → fix code → run again until all pass
```

### Phase 3: Commit & Push
```bash
# Stage all changes
git add .

# Commit with descriptive message
git commit -m "Add student enrollment feature with unit tests"

# Push feature branch to GitHub
git push origin feature/add-enrollment
```

### Phase 4: Create Pull Request
```
1. Go to GitHub → Your repository
2. Click "Compare & pull request" (yellow banner)
3. Base: main ← Compare: feature/add-enrollment
4. Write title: "Add student enrollment feature"
5. Write description: what changed and why
6. Click "Create pull request"
```

### Phase 5: CI Pipeline Runs Automatically
```
GitHub detects PR → triggers .github/workflows/ci.yml

Runner (Ubuntu VM) executes:
  ☐ Step 1: Checkout code (git clone)
  ☐ Step 2: Install JDK 17 (Temurin) + cache Maven deps
  ☐ Step 3: chmod +x ./mvnw (fix Linux permissions)
  ☐ Step 4: ./mvnw clean verify (compile + 62 tests)

Result:
  ✅ All tests pass → Green check on PR
  ❌ Any test fails → Red cross on PR → upload test reports
```

### Phase 6: Review & Merge
```
1. Check PR page → "All checks have passed" ✅
2. Click "Merge pull request"
3. Click "Confirm merge"
4. Click "Delete branch" (cleanup)
```

### Phase 7: Update Local (After Merge)
```bash
# Switch back to main
git checkout main

# Pull the merged changes
git pull origin main

# Your local main now has the new feature
```

### Complete Pipeline Diagram
```
┌─────────────────────────────────────────────────────────────────────┐
│                        LOCAL DEVELOPMENT                            │
│                                                                     │
│  Write Code → Run Tests (./mvnw test) → 62 tests pass?             │
│       │              │                      │                       │
│       │              │                 NO → Fix code                │
│       │              │                 YES ↓                        │
│       │              └──────────────────→ git add . → git commit    │
│       │                                      │                      │
│       │                                      ▼                      │
│       │                              git push origin feature/xyz    │
└───────│──────────────────────────────────────│──────────────────────┘
        │                                      │
        │                                      ▼
┌───────│──────────────────────────────────────────────────────────────┐
│       │                         GITHUB                               │
│       │                                                              │
│       │    Create Pull Request (feature/xyz → main)                  │
│       │         │                                                    │
│       │         ▼                                                    │
│       │    CI Pipeline Triggers Automatically                        │
│       │    ┌─────────────────────────────────┐                       │
│       │    │  Ubuntu VM (Runner)             │                       │
│       │    │  1. Checkout code               │                       │
│       │    │  2. Install JDK 17              │                       │
│       │    │  3. chmod +x ./mvnw             │                       │
│       │    │  4. ./mvnw clean verify         │                       │
│       │    │     ├── 62 tests pass → ✅      │                       │
│       │    │     └── Any test fails → ❌     │                       │
│       │    └─────────────────────────────────┘                       │
│       │         │                                                    │
│       │         ▼                                                    │
│       │    Status Check: build-and-test                               │
│       │    ┌─────────────────────────────────┐                       │
│       │    │  ✅ PASS → Merge button ENABLED │                       │
│       │    │  ❌ FAIL → Merge button BLOCKED │                       │
│       │    └─────────────────────────────────┘                       │
│       │         │                                                    │
│       │         ▼ (if passed)                                        │
│       │    Click "Merge pull request"                                │
│       │    main branch updated with new code                         │
└───────│──────────────────────────────────────────────────────────────┘
        │                                      │
        │                                      ▼
┌───────│──────────────────────────────────────────────────────────────┐
│       │              LOCAL (After Merge)                              │
│       │                                                              │
│       └──────→  git checkout main → git pull origin main             │
│                 Local main is now updated ✅                          │
└──────────────────────────────────────────────────────────────────────┘
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
| `.github/workflows/ci.yml` | GitHub Actions CI pipeline — triggers on push/PR to main |
| `application-test.yml` | Test profile config — H2 in-memory DB, disables data.sql & Docker |

### Java Classes
| Class | Layer | Purpose |
|---|---|---|
| `WebappApplication.java` | Entry | `main()` method — starts Spring Boot application |
| `WebappApplicationTests.java` | Test | Integration test — verifies full Spring context loads with H2 |
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
| **Workflow** | YAML file in `.github/workflows/` defining automated CI/CD tasks |
| **Runner** | GitHub-provided VM (Ubuntu) that executes workflow jobs — fresh each run |
| **Branch Protection** | Rules preventing direct pushes to main — requires PR, review, and CI checks |
| **Status Check** | CI job result that must pass before PR can merge (e.g., build-and-test) |
| **Artifact** | File saved from CI run (test reports) — downloadable for debugging |
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

> **62 tests passing ✅ (61 unit + 1 integration) | Docker containerized 🐳 | GitHub Actions CI automated ⚙️ | Branch protection active 🔒**
