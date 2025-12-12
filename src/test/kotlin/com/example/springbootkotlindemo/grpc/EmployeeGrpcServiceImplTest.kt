package com.example.springbootkotlindemo.grpc

import com.example.springbootkotlindemo.model.Employee
import com.example.springbootkotlindemo.service.EmployeeService
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EmployeeGrpcServiceImplTest : DescribeSpec({

    lateinit var employeeService: EmployeeService
    lateinit var grpcService: EmployeeGrpcServiceImpl

    beforeEach {
        employeeService = mock()
        grpcService = EmployeeGrpcServiceImpl(employeeService)
    }

    describe("EmployeeGrpcServiceImpl") {

        describe("createEmployee") {
            it("should create employee and return employee message") {
                val request = CreateEmployeeRequest.newBuilder()
                    .setFirstName("John")
                    .setLastName("Doe")
                    .setEmail("john.doe@example.com")
                    .setDepartment("Engineering")
                    .build()

                val savedEmployee = Employee(
                    id = 1L,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com",
                    department = "Engineering"
                )

                whenever(employeeService.createEmployee(any())).thenReturn(savedEmployee)

                val responseObserver = mock<StreamObserver<EmployeeMessage>>()

                grpcService.createEmployee(request, responseObserver)

                val captor = argumentCaptor<EmployeeMessage>()
                verify(responseObserver).onNext(captor.capture())
                verify(responseObserver).onCompleted()
                verify(responseObserver, never()).onError(any())

                val response = captor.firstValue
                response.id shouldBe 1L
                response.firstName shouldBe "John"
                response.lastName shouldBe "Doe"
                response.email shouldBe "john.doe@example.com"
                response.department shouldBe "Engineering"
            }
        }

        describe("getEmployee") {
            it("should return employee when found") {
                val request = GetEmployeeRequest.newBuilder()
                    .setId(1L)
                    .build()

                val employee = Employee(
                    id = 1L,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john.doe@example.com",
                    department = "Engineering"
                )

                whenever(employeeService.getEmployeeById(1L)).thenReturn(employee)

                val responseObserver = mock<StreamObserver<EmployeeMessage>>()

                grpcService.getEmployee(request, responseObserver)

                val captor = argumentCaptor<EmployeeMessage>()
                verify(responseObserver).onNext(captor.capture())
                verify(responseObserver).onCompleted()
                verify(responseObserver, never()).onError(any())

                val response = captor.firstValue
                response.id shouldBe 1L
                response.firstName shouldBe "John"
                response.lastName shouldBe "Doe"
                response.email shouldBe "john.doe@example.com"
                response.department shouldBe "Engineering"
            }

            it("should return NOT_FOUND error when employee not found") {
                val request = GetEmployeeRequest.newBuilder()
                    .setId(999L)
                    .build()

                whenever(employeeService.getEmployeeById(999L)).thenReturn(null)

                val responseObserver = mock<StreamObserver<EmployeeMessage>>()

                grpcService.getEmployee(request, responseObserver)

                verify(responseObserver, never()).onNext(any())
                verify(responseObserver, never()).onCompleted()

                val errorCaptor = argumentCaptor<Throwable>()
                verify(responseObserver).onError(errorCaptor.capture())

                val error = errorCaptor.firstValue as StatusRuntimeException
                error.status.code shouldBe Status.NOT_FOUND.code
                error.status.description shouldBe "Employee not found with id: 999"
            }
        }

        describe("getAllEmployees") {
            it("should return all employees") {
                val request = GetAllEmployeesRequest.newBuilder().build()

                val employees = listOf(
                    Employee(id = 1L, firstName = "John", lastName = "Doe", email = "john@example.com", department = "IT"),
                    Employee(id = 2L, firstName = "Jane", lastName = "Smith", email = "jane@example.com", department = "HR")
                )

                whenever(employeeService.getAllEmployees()).thenReturn(employees)

                val responseObserver = mock<StreamObserver<GetAllEmployeesResponse>>()

                grpcService.getAllEmployees(request, responseObserver)

                val captor = argumentCaptor<GetAllEmployeesResponse>()
                verify(responseObserver).onNext(captor.capture())
                verify(responseObserver).onCompleted()
                verify(responseObserver, never()).onError(any())

                val response = captor.firstValue
                response.employeesCount shouldBe 2
                response.employeesList[0].id shouldBe 1L
                response.employeesList[0].firstName shouldBe "John"
                response.employeesList[1].id shouldBe 2L
                response.employeesList[1].firstName shouldBe "Jane"
            }

            it("should return empty list when no employees exist") {
                val request = GetAllEmployeesRequest.newBuilder().build()

                whenever(employeeService.getAllEmployees()).thenReturn(emptyList())

                val responseObserver = mock<StreamObserver<GetAllEmployeesResponse>>()

                grpcService.getAllEmployees(request, responseObserver)

                val captor = argumentCaptor<GetAllEmployeesResponse>()
                verify(responseObserver).onNext(captor.capture())
                verify(responseObserver).onCompleted()

                val response = captor.firstValue
                response.employeesCount shouldBe 0
            }
        }

        describe("updateEmployee") {
            it("should update and return employee when found") {
                val request = UpdateEmployeeRequest.newBuilder()
                    .setId(1L)
                    .setFirstName("John")
                    .setLastName("Updated")
                    .setEmail("john.updated@example.com")
                    .setDepartment("Management")
                    .build()

                val updatedEmployee = Employee(
                    id = 1L,
                    firstName = "John",
                    lastName = "Updated",
                    email = "john.updated@example.com",
                    department = "Management"
                )

                whenever(employeeService.updateEmployee(any(), any())).thenReturn(updatedEmployee)

                val responseObserver = mock<StreamObserver<EmployeeMessage>>()

                grpcService.updateEmployee(request, responseObserver)

                val captor = argumentCaptor<EmployeeMessage>()
                verify(responseObserver).onNext(captor.capture())
                verify(responseObserver).onCompleted()
                verify(responseObserver, never()).onError(any())

                val response = captor.firstValue
                response.id shouldBe 1L
                response.firstName shouldBe "John"
                response.lastName shouldBe "Updated"
                response.email shouldBe "john.updated@example.com"
                response.department shouldBe "Management"
            }

            it("should return NOT_FOUND error when employee not found") {
                val request = UpdateEmployeeRequest.newBuilder()
                    .setId(999L)
                    .setFirstName("John")
                    .setLastName("Updated")
                    .setEmail("john.updated@example.com")
                    .setDepartment("Management")
                    .build()

                whenever(employeeService.updateEmployee(any(), any())).thenReturn(null)

                val responseObserver = mock<StreamObserver<EmployeeMessage>>()

                grpcService.updateEmployee(request, responseObserver)

                verify(responseObserver, never()).onNext(any())
                verify(responseObserver, never()).onCompleted()

                val errorCaptor = argumentCaptor<Throwable>()
                verify(responseObserver).onError(errorCaptor.capture())

                val error = errorCaptor.firstValue as StatusRuntimeException
                error.status.code shouldBe Status.NOT_FOUND.code
                error.status.description shouldBe "Employee not found with id: 999"
            }
        }

        describe("deleteEmployee") {
            it("should return success when employee deleted") {
                val request = DeleteEmployeeRequest.newBuilder()
                    .setId(1L)
                    .build()

                whenever(employeeService.deleteEmployee(1L)).thenReturn(true)

                val responseObserver = mock<StreamObserver<DeleteEmployeeResponse>>()

                grpcService.deleteEmployee(request, responseObserver)

                val captor = argumentCaptor<DeleteEmployeeResponse>()
                verify(responseObserver).onNext(captor.capture())
                verify(responseObserver).onCompleted()
                verify(responseObserver, never()).onError(any())

                val response = captor.firstValue
                response.success shouldBe true
                response.message shouldBe "Employee deleted successfully"
            }

            it("should return failure when employee not found") {
                val request = DeleteEmployeeRequest.newBuilder()
                    .setId(999L)
                    .build()

                whenever(employeeService.deleteEmployee(999L)).thenReturn(false)

                val responseObserver = mock<StreamObserver<DeleteEmployeeResponse>>()

                grpcService.deleteEmployee(request, responseObserver)

                val captor = argumentCaptor<DeleteEmployeeResponse>()
                verify(responseObserver).onNext(captor.capture())
                verify(responseObserver).onCompleted()
                verify(responseObserver, never()).onError(any())

                val response = captor.firstValue
                response.success shouldBe false
                response.message shouldBe "Employee not found with id: 999"
            }
        }
    }
})
