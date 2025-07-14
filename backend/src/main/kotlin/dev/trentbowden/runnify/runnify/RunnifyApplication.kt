package dev.trentbowden.runnify.runnify

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RunnifyApplication

fun main(args: Array<String>) {
	runApplication<RunnifyApplication>(*args)
}
