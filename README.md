# Spring Boot Kotlin Demo

A RESTful Employee Management API built with Spring Boot and Kotlin, featuring H2 database persistence and Kafka event publishing for CRUD operations.

## Overview

This project demonstrates a modern Spring Boot application written in Kotlin that provides:
- RESTful API endpoints for Employee CRUD operations
- H2 in-memory database for data persistence
- Kafka event publishing for each operation (Create, Read, Update, Delete)
- Comprehensive unit and integration tests using Kotest and Mockito

## Technologies

- **Kotlin** 2.2.21
- **Spring Boot** 4.0.0
- **Spring Data JPA** - Database persistence
- **Spring Kafka** - Event publishing
- **H2 Database** - In-memory database
- **WebFlux** - Reactive web support
- **gRPC** - Remote procedure calls
- **OpenTelemetry** - Observability
- **Kotest** - Testing framework
- **Mockito** - Mocking framework
- **Testcontainers** - Integration testing

## Prerequisites

- **Java 21** or higher
- **Docker** (for running Kafka and other services)
- **Gradle** (wrapper included)

## Getting Started

### Clone the Repository

```bash
git clone <repository-url>
cd springboot-kotlin-demo
```

### Running with Docker Compose

The project includes a `compose.yaml` file for setting up required services:

```bash
docker-compose up -d
```

### Running the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

### Accessing H2 Console

The H2 database console is available at `http://localhost:8080/h2-console`:
- **JDBC URL:** `jdbc:h2:mem:employeedb`
- **Username:** `sa`
- **Password:** (empty)

## API Endpoints

### Employee REST API

Base URL: `/api/employees`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/employees` | Create a new employee |
| GET | `/api/employees/{id}` | Get employee by ID |
| GET | `/api/employees` | Get all employees |
| PUT | `/api/employees/{id}` | Update an employee |
| DELETE | `/api/employees/{id}` | Delete an employee |

### Request/Response Examples

#### Create Employee
```bash
POST /api/employees
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "department": "Engineering"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "department": "Engineering"
}
```

#### Get Employee by ID
```bash
GET /api/employees/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "department": "Engineering"
}
```

#### Update Employee
```bash
PUT /api/employees/1
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "email": "john.smith@example.com",
  "department": "Management"
}
```

#### Delete Employee
```bash
DELETE /api/employees/1
```

**Response (204 No Content)**

### Employee gRPC API

The application also provides gRPC endpoints for Employee CRUD operations on port `9090`.

#### Proto Definition

The gRPC service is defined in `src/main/proto/employee.proto`:

| Method | Request | Response | Description |
|--------|---------|----------|-------------|
| CreateEmployee | CreateEmployeeRequest | EmployeeMessage | Create a new employee |
| GetEmployee | GetEmployeeRequest | EmployeeMessage | Get employee by ID |
| GetAllEmployees | GetAllEmployeesRequest | GetAllEmployeesResponse | Get all employees |
| UpdateEmployee | UpdateEmployeeRequest | EmployeeMessage | Update an employee |
| DeleteEmployee | DeleteEmployeeRequest | DeleteEmployeeResponse | Delete an employee |

#### gRPC Message Types

**EmployeeMessage:**
```protobuf
message EmployeeMessage {
  int64 id = 1;
  string first_name = 2;
  string last_name = 3;
  string email = 4;
  string department = 5;
}
```

**CreateEmployeeRequest:**
```protobuf
message CreateEmployeeRequest {
  string first_name = 1;
  string last_name = 2;
  string email = 3;
  string department = 4;
}
```

**DeleteEmployeeResponse:**
```protobuf
message DeleteEmployeeResponse {
  bool success = 1;
  string message = 2;
}
```

#### gRPC Usage Examples

Using `grpcurl` to interact with the gRPC API:

**Create Employee:**
```bash
grpcurl -plaintext -d '{
  "first_name": "John",
  "last_name": "Doe",
  "email": "john.doe@example.com",
  "department": "Engineering"
}' localhost:9090 com.example.springbootkotlindemo.grpc.EmployeeGrpcService/CreateEmployee
```

**Get Employee by ID:**
```bash
grpcurl -plaintext -d '{"id": 1}' localhost:9090 com.example.springbootkotlindemo.grpc.EmployeeGrpcService/GetEmployee
```

**Get All Employees:**
```bash
grpcurl -plaintext localhost:9090 com.example.springbootkotlindemo.grpc.EmployeeGrpcService/GetAllEmployees
```

**Update Employee:**
```bash
grpcurl -plaintext -d '{
  "id": 1,
  "first_name": "John",
  "last_name": "Smith",
  "email": "john.smith@example.com",
  "department": "Management"
}' localhost:9090 com.example.springbootkotlindemo.grpc.EmployeeGrpcService/UpdateEmployee
```

**Delete Employee:**
```bash
grpcurl -plaintext -d '{"id": 1}' localhost:9090 com.example.springbootkotlindemo.grpc.EmployeeGrpcService/DeleteEmployee
```

## Kafka Events

Every CRUD operation publishes an event to the `employee-events` Kafka topic. Event structure:

```json
{
  "operation": "CREATE|READ|UPDATE|DELETE",
  "employee": { ... },
  "employeeId": 1,
  "message": "Operation result message"
}
```

### Operation Types
- **CREATE** - Published when a new employee is created
- **READ** - Published when an employee is retrieved
- **UPDATE** - Published when an employee is updated
- **DELETE** - Published when an employee is deleted

## Configuration

### Application Properties

Key configuration in `src/main/resources/application.properties`:

```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:employeedb
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

## Testing

The project includes comprehensive tests using Kotest and Mockito:

### Run All Tests

```bash
./gradlew test
```

### Test Categories

- **Unit Tests** - Located in `src/test/kotlin/.../service/` and `src/test/kotlin/.../controller/`
  - `EmployeeServiceTest` - Tests for EmployeeService business logic
  - `EmployeeEventServiceTest` - Tests for Kafka event publishing
  - `EmployeeControllerTest` - Tests for REST controller endpoints

- **gRPC Tests** - Located in `src/test/kotlin/.../grpc/`
  - `EmployeeGrpcServiceImplTest` - Tests for gRPC service endpoints (Create, Get, GetAll, Update, Delete)

- **Integration Tests** - Located in `src/test/kotlin/.../`
  - `EmployeeIntegrationTest` - Full API integration tests with Spring context

### Testing Frameworks
- **Kotest** - BDD-style test specifications with `DescribeSpec`
- **Mockito** - Mocking dependencies for unit tests
- **Testcontainers** - Docker containers for integration testing

## Project Structure

```
springboot-kotlin-demo/
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/springbootkotlindemo/
│   │   │   ├── controller/
│   │   │   │   └── EmployeeController.kt      # REST API endpoints
│   │   │   ├── event/
│   │   │   │   └── EmployeeEvent.kt           # Kafka event model
│   │   │   ├── grpc/
│   │   │   │   └── EmployeeGrpcServiceImpl.kt # gRPC service implementation
│   │   │   ├── model/
│   │   │   │   └── Employee.kt                # JPA entity
│   │   │   ├── repository/
│   │   │   │   └── EmployeeRepository.kt      # Data access layer
│   │   │   ├── service/
│   │   │   │   ├── EmployeeService.kt         # Business logic
│   │   │   │   └── EmployeeEventService.kt    # Kafka event publishing
│   │   │   └── SpringbootKotlinDemoApplication.kt
│   │   ├── proto/
│   │   │   └── employee.proto                 # gRPC service definition
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── kotlin/com/example/springbootkotlindemo/
│       │   ├── controller/
│       │   │   └── EmployeeControllerTest.kt
│       │   ├── grpc/
│       │   │   └── EmployeeGrpcServiceImplTest.kt # gRPC service tests
│       │   ├── service/
│       │   │   ├── EmployeeServiceTest.kt
│       │   │   └── EmployeeEventServiceTest.kt
│       │   ├── EmployeeIntegrationTest.kt
│       │   └── TestcontainersConfiguration.kt
│       └── resources/
│           └── application-test.properties
├── build.gradle.kts
├── compose.yaml
├── settings.gradle.kts
└── README.md
```

## Build

### Build the Project

```bash
./gradlew build
```

### Create Executable JAR

```bash
./gradlew bootJar
```

The JAR file will be created in `build/libs/`.

## License

This project is for demonstration purposes.
