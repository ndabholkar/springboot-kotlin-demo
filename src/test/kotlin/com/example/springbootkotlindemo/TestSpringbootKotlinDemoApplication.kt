package com.example.springbootkotlindemo

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<SpringbootKotlinDemoApplication>().with(TestcontainersConfiguration::class).run(*args)
}
