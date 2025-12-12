package com.example.springbootkotlindemo.service

import com.example.springbootkotlindemo.model.Employee
import com.example.springbootkotlindemo.repository.EmployeeRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

class EmployeeServiceTest : DescribeSpec({

    lateinit var employeeRepository: EmployeeRepository
    lateinit var employeeEventService: EmployeeEventService
    lateinit var employeeService: EmployeeService

    beforeEach {
        employeeRepository = mock()
        employeeEventService = mock()
        employeeService = EmployeeService(employeeRepository, employeeEventService)
    }

    describe("EmployeeService") {

        describe("createEmployee") {
            it("should save employee and publish create event") {
                val employee = Employee(firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")
                val savedEmployee = employee.copy(id = 1L)

                whenever(employeeRepository.save(employee)).thenReturn(savedEmployee)

                val result = employeeService.createEmployee(employee)

                result shouldBe savedEmployee
                verify(employeeRepository).save(employee)
                verify(employeeEventService).publishCreateEvent(savedEmployee)
            }
        }

        describe("getEmployeeById") {
            it("should return employee when found") {
                val employee = Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")

                whenever(employeeRepository.findById(1L)).thenReturn(Optional.of(employee))

                val result = employeeService.getEmployeeById(1L)

                result shouldNotBe null
                result shouldBe employee
                verify(employeeRepository).findById(1L)
                verify(employeeEventService).publishReadEvent(employee)
            }

            it("should return null when not found") {
                whenever(employeeRepository.findById(1L)).thenReturn(Optional.empty())

                val result = employeeService.getEmployeeById(1L)

                result shouldBe null
                verify(employeeRepository).findById(1L)
                verify(employeeEventService, never()).publishReadEvent(any())
            }
        }

        describe("getAllEmployees") {
            it("should return all employees and publish read all event") {
                val employees = listOf(
                    Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT"),
                    Employee(id = 2L, firstName = "Jane", lastName = "Smith", email = "jane@example.com", department = "HR")
                )

                whenever(employeeRepository.findAll()).thenReturn(employees)

                val result = employeeService.getAllEmployees()

                result.size shouldBe 2
                result shouldBe employees
                verify(employeeRepository).findAll()
                verify(employeeEventService).publishReadAllEvent(employees)
            }
        }

        describe("updateEmployee") {
            it("should update and return employee when found") {
                val existingEmployee = Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")
                val employeeDetails = Employee(firstName = "John", lastName = "Updated", email = "john.updated@example.com", department = "Engineering")
                val updatedEmployee = existingEmployee.copy(
                    firstName = employeeDetails.firstName,
                    lastName = employeeDetails.lastName,
                    email = employeeDetails.email,
                    department = employeeDetails.department
                )

                whenever(employeeRepository.findById(1L)).thenReturn(Optional.of(existingEmployee))
                whenever(employeeRepository.save(updatedEmployee)).thenReturn(updatedEmployee)

                val result = employeeService.updateEmployee(1L, employeeDetails)

                result shouldNotBe null
                result shouldBe updatedEmployee
                verify(employeeRepository).findById(1L)
                verify(employeeRepository).save(updatedEmployee)
                verify(employeeEventService).publishUpdateEvent(updatedEmployee)
            }

            it("should return null when employee not found") {
                val employeeDetails = Employee(firstName = "John", lastName = "Updated", email = "john.updated@example.com", department = "Engineering")

                whenever(employeeRepository.findById(1L)).thenReturn(Optional.empty())

                val result = employeeService.updateEmployee(1L, employeeDetails)

                result shouldBe null
                verify(employeeRepository).findById(1L)
                verify(employeeRepository, never()).save(any())
                verify(employeeEventService, never()).publishUpdateEvent(any())
            }
        }

        describe("deleteEmployee") {
            it("should return true and publish delete event when employee exists") {
                whenever(employeeRepository.existsById(1L)).thenReturn(true)

                val result = employeeService.deleteEmployee(1L)

                result shouldBe true
                verify(employeeRepository).existsById(1L)
                verify(employeeRepository).deleteById(1L)
                verify(employeeEventService).publishDeleteEvent(1L)
            }

            it("should return false when employee does not exist") {
                whenever(employeeRepository.existsById(1L)).thenReturn(false)

                val result = employeeService.deleteEmployee(1L)

                result shouldBe false
                verify(employeeRepository).existsById(1L)
                verify(employeeRepository, never()).deleteById(any())
                verify(employeeEventService, never()).publishDeleteEvent(any())
            }
        }
    }
})
