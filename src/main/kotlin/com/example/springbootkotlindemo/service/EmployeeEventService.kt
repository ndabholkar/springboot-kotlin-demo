package com.example.springbootkotlindemo.service

import com.example.springbootkotlindemo.event.EmployeeEvent
import com.example.springbootkotlindemo.event.OperationType
import com.example.springbootkotlindemo.model.Employee
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EmployeeEventService(
    private val kafkaTemplate: KafkaTemplate<String, EmployeeEvent>
) {
    private val logger = LoggerFactory.getLogger(EmployeeEventService::class.java)

    companion object {
        const val TOPIC = "employee-events"
    }

    fun publishCreateEvent(employee: Employee) {
        val event = EmployeeEvent(
            operation = OperationType.CREATE,
            employee = employee,
            employeeId = employee.id,
            message = "Employee created successfully"
        )
        sendEvent(event)
    }

    fun publishReadEvent(employee: Employee) {
        val event = EmployeeEvent(
            operation = OperationType.READ,
            employee = employee,
            employeeId = employee.id,
            message = "Employee retrieved successfully"
        )
        sendEvent(event)
    }

    fun publishReadAllEvent(employees: List<Employee>) {
        val event = EmployeeEvent(
            operation = OperationType.READ,
            employee = null,
            employeeId = null,
            message = "Retrieved ${employees.size} employees"
        )
        sendEvent(event)
    }

    fun publishUpdateEvent(employee: Employee) {
        val event = EmployeeEvent(
            operation = OperationType.UPDATE,
            employee = employee,
            employeeId = employee.id,
            message = "Employee updated successfully"
        )
        sendEvent(event)
    }

    fun publishDeleteEvent(employeeId: Long) {
        val event = EmployeeEvent(
            operation = OperationType.DELETE,
            employee = null,
            employeeId = employeeId,
            message = "Employee deleted successfully"
        )
        sendEvent(event)
    }

    private fun sendEvent(event: EmployeeEvent) {
        try {
            kafkaTemplate.send(TOPIC, event.employeeId?.toString() ?: "all", event)
            logger.info("Published event: $event")
        } catch (e: Exception) {
            logger.error("Failed to publish event: $event", e)
        }
    }
}
