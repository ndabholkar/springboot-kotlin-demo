package com.example.springbootkotlindemo.grpc

import com.example.springbootkotlindemo.model.Employee
import com.example.springbootkotlindemo.service.EmployeeService
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class EmployeeGrpcServiceImpl(
    private val employeeService: EmployeeService
) : EmployeeGrpcServiceGrpc.EmployeeGrpcServiceImplBase() {

    override fun createEmployee(
        request: CreateEmployeeRequest,
        responseObserver: StreamObserver<EmployeeMessage>
    ) {
        val employee = Employee(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            department = request.department
        )
        
        val createdEmployee = employeeService.createEmployee(employee)
        val response = toEmployeeMessage(createdEmployee)
        
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getEmployee(
        request: GetEmployeeRequest,
        responseObserver: StreamObserver<EmployeeMessage>
    ) {
        val employee = employeeService.getEmployeeById(request.id)
        
        if (employee != null) {
            val response = toEmployeeMessage(employee)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } else {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("Employee not found with id: ${request.id}")
                    .asRuntimeException()
            )
        }
    }

    override fun getAllEmployees(
        request: GetAllEmployeesRequest,
        responseObserver: StreamObserver<GetAllEmployeesResponse>
    ) {
        val employees = employeeService.getAllEmployees()
        
        val response = GetAllEmployeesResponse.newBuilder()
            .addAllEmployees(employees.map { toEmployeeMessage(it) })
            .build()
        
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun updateEmployee(
        request: UpdateEmployeeRequest,
        responseObserver: StreamObserver<EmployeeMessage>
    ) {
        val employeeDetails = Employee(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            department = request.department
        )
        
        val updatedEmployee = employeeService.updateEmployee(request.id, employeeDetails)
        
        if (updatedEmployee != null) {
            val response = toEmployeeMessage(updatedEmployee)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } else {
            responseObserver.onError(
                Status.NOT_FOUND
                    .withDescription("Employee not found with id: ${request.id}")
                    .asRuntimeException()
            )
        }
    }

    override fun deleteEmployee(
        request: DeleteEmployeeRequest,
        responseObserver: StreamObserver<DeleteEmployeeResponse>
    ) {
        val deleted = employeeService.deleteEmployee(request.id)
        
        val response = DeleteEmployeeResponse.newBuilder()
            .setSuccess(deleted)
            .setMessage(
                if (deleted) "Employee deleted successfully"
                else "Employee not found with id: ${request.id}"
            )
            .build()
        
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun toEmployeeMessage(employee: Employee): EmployeeMessage {
        return EmployeeMessage.newBuilder()
            .setId(employee.id ?: 0)
            .setFirstName(employee.firstName)
            .setLastName(employee.lastName)
            .setEmail(employee.email)
            .setDepartment(employee.department)
            .build()
    }
}
