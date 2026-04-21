package com.veridyl.eventez

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class EventezApplication

fun main(args: Array<String>) {
    runApplication<EventezApplication>(*args)
}