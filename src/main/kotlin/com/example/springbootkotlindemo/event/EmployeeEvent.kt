package com.example.springbootkotlindemo.event

import com.example.springbootkotlindemo.model.Employee

data class EmployeeEvent(
    val operation: OperationType,
    val employee: Employee?,
    val employeeId: Long? = null,
    val message: String
)

enum class OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE
}
