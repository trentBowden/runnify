package dev.trentbowden.runnify

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class RunnifyApplication

fun main(args: Array<String>) {
    println("TESTING RESTART - " + System.currentTimeMillis())

    runApplication<RunnifyApplication>(*args)
}
