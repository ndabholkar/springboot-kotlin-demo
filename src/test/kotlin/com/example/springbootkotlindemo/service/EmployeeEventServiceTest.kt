package com.example.springbootkotlindemo.service

import com.example.springbootkotlindemo.event.EmployeeEvent
import com.example.springbootkotlindemo.event.OperationType
import com.example.springbootkotlindemo.model.Employee
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture

class EmployeeEventServiceTest : DescribeSpec({

    lateinit var kafkaTemplate: KafkaTemplate<String, EmployeeEvent>
    lateinit var employeeEventService: EmployeeEventService

    beforeEach {
        kafkaTemplate = mock()
        employeeEventService = EmployeeEventService(kafkaTemplate)
        whenever(kafkaTemplate.send(any<String>(), any<String>(), any())).thenReturn(CompletableFuture.completedFuture(null))
    }

    describe("EmployeeEventService") {

        describe("publishCreateEvent") {
            it("should publish create event to Kafka") {
                val employee = Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")

                employeeEventService.publishCreateEvent(employee)

                val eventCaptor = argumentCaptor<EmployeeEvent>()
                verify(kafkaTemplate).send(eq("employee-events"), eq("1"), eventCaptor.capture())
                
                val capturedEvent = eventCaptor.firstValue
                capturedEvent.operation shouldBe OperationType.CREATE
                capturedEvent.employee shouldBe employee
                capturedEvent.employeeId shouldBe 1L
                capturedEvent.message shouldBe "Employee created successfully"
            }
        }

        describe("publishReadEvent") {
            it("should publish read event to Kafka") {
                val employee = Employee(id = 2L, firstName = "Jane", lastName = "Smith", email = "jane@example.com", department = "HR")

                employeeEventService.publishReadEvent(employee)

                val eventCaptor = argumentCaptor<EmployeeEvent>()
                verify(kafkaTemplate).send(eq("employee-events"), eq("2"), eventCaptor.capture())
                
                val capturedEvent = eventCaptor.firstValue
                capturedEvent.operation shouldBe OperationType.READ
                capturedEvent.employee shouldBe employee
                capturedEvent.employeeId shouldBe 2L
                capturedEvent.message shouldBe "Employee retrieved successfully"
            }
        }

        describe("publishReadAllEvent") {
            it("should publish read all event to Kafka") {
                val employees = listOf(
                    Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT"),
                    Employee(id = 2L, firstName = "Jane", lastName = "Smith", email = "jane@example.com", department = "HR")
                )

                employeeEventService.publishReadAllEvent(employees)

                val eventCaptor = argumentCaptor<EmployeeEvent>()
                verify(kafkaTemplate).send(eq("employee-events"), eq("all"), eventCaptor.capture())
                
                val capturedEvent = eventCaptor.firstValue
                capturedEvent.operation shouldBe OperationType.READ
                capturedEvent.employee shouldBe null
                capturedEvent.employeeId shouldBe null
                capturedEvent.message shouldBe "Retrieved 2 employees"
            }
        }

        describe("publishUpdateEvent") {
            it("should publish update event to Kafka") {
                val employee = Employee(id = 3L, firstName = "Bob", lastName = "Johnson", email = "bob@example.com", department = "Finance")

                employeeEventService.publishUpdateEvent(employee)

                val eventCaptor = argumentCaptor<EmployeeEvent>()
                verify(kafkaTemplate).send(eq("employee-events"), eq("3"), eventCaptor.capture())
                
                val capturedEvent = eventCaptor.firstValue
                capturedEvent.operation shouldBe OperationType.UPDATE
                capturedEvent.employee shouldBe employee
                capturedEvent.employeeId shouldBe 3L
                capturedEvent.message shouldBe "Employee updated successfully"
            }
        }

        describe("publishDeleteEvent") {
            it("should publish delete event to Kafka") {
                employeeEventService.publishDeleteEvent(5L)

                val eventCaptor = argumentCaptor<EmployeeEvent>()
                verify(kafkaTemplate).send(eq("employee-events"), eq("5"), eventCaptor.capture())
                
                val capturedEvent = eventCaptor.firstValue
                capturedEvent.operation shouldBe OperationType.DELETE
                capturedEvent.employee shouldBe null
                capturedEvent.employeeId shouldBe 5L
                capturedEvent.message shouldBe "Employee deleted successfully"
            }
        }
    }
})
