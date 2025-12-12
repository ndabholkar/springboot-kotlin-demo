package com.example.springbootkotlindemo

import com.example.springbootkotlindemo.event.EmployeeEvent
import com.example.springbootkotlindemo.model.Employee
import com.example.springbootkotlindemo.repository.EmployeeRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.extensions.spring.SpringExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.util.concurrent.CompletableFuture

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EmployeeIntegrationTest(
    @LocalServerPort private val port: Int,
    private val employeeRepository: EmployeeRepository
) : DescribeSpec() {

    override fun extensions() = listOf(SpringExtension)

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun kafkaTemplate(): KafkaTemplate<String, EmployeeEvent> {
            val mock = org.mockito.kotlin.mock<KafkaTemplate<String, EmployeeEvent>>()
            whenever(mock.send(any<String>(), any<String>(), any())).thenReturn(CompletableFuture.completedFuture(null))
            return mock
        }
    }

    init {
        lateinit var webTestClient: WebTestClient

        beforeSpec {
            webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
        }

        beforeEach {
            employeeRepository.deleteAll()
        }

        describe("Employee REST API Integration Tests") {

            describe("POST /api/employees") {
                it("should create a new employee") {
                    val employee = Employee(
                        firstName = "John",
                        lastName = "Doe",
                        email = "john.doe@example.com",
                        department = "Engineering"
                    )

                    webTestClient.post()
                        .uri("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(employee)
                        .exchange()
                        .expectStatus().isCreated
                        .expectBody<Employee>()
                        .consumeWith { response ->
                            val createdEmployee = response.responseBody!!
                            createdEmployee.id shouldNotBe null
                            createdEmployee.firstName shouldBe "John"
                            createdEmployee.lastName shouldBe "Doe"
                            createdEmployee.email shouldBe "john.doe@example.com"
                            createdEmployee.department shouldBe "Engineering"
                        }
                }
            }

            describe("GET /api/employees/{id}") {
                it("should return employee when found") {
                    val savedEmployee = employeeRepository.save(
                        Employee(
                            firstName = "Jane",
                            lastName = "Smith",
                            email = "jane.smith@example.com",
                            department = "HR"
                        )
                    )

                    webTestClient.get()
                        .uri("/api/employees/${savedEmployee.id}")
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<Employee>()
                        .consumeWith { response ->
                            val employee = response.responseBody!!
                            employee.id shouldBe savedEmployee.id
                            employee.firstName shouldBe "Jane"
                            employee.lastName shouldBe "Smith"
                        }
                }

                it("should return 404 when employee not found") {
                    webTestClient.get()
                        .uri("/api/employees/999")
                        .exchange()
                        .expectStatus().isNotFound
                }
            }

            describe("GET /api/employees") {
                it("should return all employees") {
                    employeeRepository.save(
                        Employee(firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")
                    )
                    employeeRepository.save(
                        Employee(firstName = "Jane", lastName = "Smith", email = "jane@example.com", department = "HR")
                    )

                    webTestClient.get()
                        .uri("/api/employees")
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<List<Employee>>()
                        .consumeWith { response ->
                            val employees = response.responseBody!!
                            employees.size shouldBe 2
                        }
                }

                it("should return empty list when no employees") {
                    webTestClient.get()
                        .uri("/api/employees")
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<List<Employee>>()
                        .consumeWith { response ->
                            val employees = response.responseBody!!
                            employees.size shouldBe 0
                        }
                }
            }

            describe("PUT /api/employees/{id}") {
                it("should update employee when found") {
                    val savedEmployee = employeeRepository.save(
                        Employee(firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")
                    )

                    val updateRequest = Employee(
                        firstName = "John",
                        lastName = "Updated",
                        email = "john.updated@example.com",
                        department = "Engineering"
                    )

                    webTestClient.put()
                        .uri("/api/employees/${savedEmployee.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updateRequest)
                        .exchange()
                        .expectStatus().isOk
                        .expectBody<Employee>()
                        .consumeWith { response ->
                            val updatedEmployee = response.responseBody!!
                            updatedEmployee.id shouldBe savedEmployee.id
                            updatedEmployee.lastName shouldBe "Updated"
                            updatedEmployee.email shouldBe "john.updated@example.com"
                            updatedEmployee.department shouldBe "Engineering"
                        }
                }

                it("should return 404 when updating non-existent employee") {
                    val updateRequest = Employee(
                        firstName = "John",
                        lastName = "Updated",
                        email = "john.updated@example.com",
                        department = "Engineering"
                    )

                    webTestClient.put()
                        .uri("/api/employees/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updateRequest)
                        .exchange()
                        .expectStatus().isNotFound
                }
            }

            describe("DELETE /api/employees/{id}") {
                it("should delete employee when found") {
                    val savedEmployee = employeeRepository.save(
                        Employee(firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")
                    )

                    webTestClient.delete()
                        .uri("/api/employees/${savedEmployee.id}")
                        .exchange()
                        .expectStatus().isNoContent

                    // Verify employee is deleted
                    webTestClient.get()
                        .uri("/api/employees/${savedEmployee.id}")
                        .exchange()
                        .expectStatus().isNotFound
                }

                it("should return 404 when deleting non-existent employee") {
                    webTestClient.delete()
                        .uri("/api/employees/999")
                        .exchange()
                        .expectStatus().isNotFound
                }
            }
        }
    }
}
