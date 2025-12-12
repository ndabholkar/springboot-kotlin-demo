package com.example.springbootkotlindemo.controller

import com.example.springbootkotlindemo.model.Employee
import com.example.springbootkotlindemo.service.EmployeeService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus

class EmployeeControllerTest : DescribeSpec({

    lateinit var employeeService: EmployeeService
    lateinit var employeeController: EmployeeController

    beforeEach {
        employeeService = mock()
        employeeController = EmployeeController(employeeService)
    }

    describe("EmployeeController") {

        describe("createEmployee") {
            it("should create employee and return CREATED status") {
                val employee = Employee(firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")
                val savedEmployee = employee.copy(id = 1L)

                whenever(employeeService.createEmployee(employee)).thenReturn(savedEmployee)

                val response = employeeController.createEmployee(employee)

                response.statusCode shouldBe HttpStatus.CREATED
                response.body shouldBe savedEmployee
                verify(employeeService).createEmployee(employee)
            }
        }

        describe("getEmployeeById") {
            it("should return employee when found") {
                val employee = Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT")

                whenever(employeeService.getEmployeeById(1L)).thenReturn(employee)

                val response = employeeController.getEmployeeById(1L)

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldBe employee
                verify(employeeService).getEmployeeById(1L)
            }

            it("should return NOT_FOUND when employee does not exist") {
                whenever(employeeService.getEmployeeById(1L)).thenReturn(null)

                val response = employeeController.getEmployeeById(1L)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                verify(employeeService).getEmployeeById(1L)
            }
        }

        describe("getAllEmployees") {
            it("should return all employees") {
                val employees = listOf(
                    Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT"),
                    Employee(id = 2L, firstName = "Jane", lastName = "Smith", email = "jane@example.com", department = "HR")
                )

                whenever(employeeService.getAllEmployees()).thenReturn(employees)

                val response = employeeController.getAllEmployees()

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldBe employees
                verify(employeeService).getAllEmployees()
            }

            it("should return empty list when no employees exist") {
                whenever(employeeService.getAllEmployees()).thenReturn(emptyList())

                val response = employeeController.getAllEmployees()

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldBe emptyList()
                verify(employeeService).getAllEmployees()
            }
        }

        describe("updateEmployee") {
            it("should update employee and return OK when found") {
                val employeeDetails = Employee(firstName = "John", lastName = "Updated", email = "john.updated@example.com", department = "Engineering")
                val updatedEmployee = employeeDetails.copy(id = 1L)

                whenever(employeeService.updateEmployee(1L, employeeDetails)).thenReturn(updatedEmployee)

                val response = employeeController.updateEmployee(1L, employeeDetails)

                response.statusCode shouldBe HttpStatus.OK
                response.body shouldBe updatedEmployee
                verify(employeeService).updateEmployee(1L, employeeDetails)
            }

            it("should return NOT_FOUND when employee does not exist") {
                val employeeDetails = Employee(firstName = "John", lastName = "Updated", email = "john.updated@example.com", department = "Engineering")

                whenever(employeeService.updateEmployee(1L, employeeDetails)).thenReturn(null)

                val response = employeeController.updateEmployee(1L, employeeDetails)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                verify(employeeService).updateEmployee(1L, employeeDetails)
            }
        }

        describe("deleteEmployee") {
            it("should delete employee and return NO_CONTENT when found") {
                whenever(employeeService.deleteEmployee(1L)).thenReturn(true)

                val response = employeeController.deleteEmployee(1L)

                response.statusCode shouldBe HttpStatus.NO_CONTENT
                verify(employeeService).deleteEmployee(1L)
            }

            it("should return NOT_FOUND when employee does not exist") {
                whenever(employeeService.deleteEmployee(1L)).thenReturn(false)

                val response = employeeController.deleteEmployee(1L)

                response.statusCode shouldBe HttpStatus.NOT_FOUND
                verify(employeeService).deleteEmployee(1L)
            }
        }
    }
})
