package com.example.springbootkotlindemo.service

import com.example.springbootkotlindemo.model.Employee
import com.example.springbootkotlindemo.repository.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val employeeEventService: EmployeeEventService
) {

    fun createEmployee(employee: Employee): Employee {
        val savedEmployee = employeeRepository.save(employee)
        employeeEventService.publishCreateEvent(savedEmployee)
        return savedEmployee
    }

    @Transactional(readOnly = true)
    fun getEmployeeById(id: Long): Employee? {
        val employee = employeeRepository.findById(id).orElse(null)
        employee?.let { employeeEventService.publishReadEvent(it) }
        return employee
    }

    @Transactional(readOnly = true)
    fun getAllEmployees(): List<Employee> {
        val employees = employeeRepository.findAll()
        employeeEventService.publishReadAllEvent(employees)
        return employees
    }

    fun updateEmployee(id: Long, employeeDetails: Employee): Employee? {
        val existingEmployee = employeeRepository.findById(id).orElse(null) ?: return null
        
        val updatedEmployee = existingEmployee.copy(
            firstName = employeeDetails.firstName,
            lastName = employeeDetails.lastName,
            email = employeeDetails.email,
            department = employeeDetails.department
        )
        
        val savedEmployee = employeeRepository.save(updatedEmployee)
        employeeEventService.publishUpdateEvent(savedEmployee)
        return savedEmployee
    }

    fun deleteEmployee(id: Long): Boolean {
        if (!employeeRepository.existsById(id)) {
            return false
        }
        employeeRepository.deleteById(id)
        employeeEventService.publishDeleteEvent(id)
        return true
    }
}
