package com.veridyl.eventez

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventezApplication

fun main(args: Array<String>) {
    runApplication<EventezApplication>(*args)
}
