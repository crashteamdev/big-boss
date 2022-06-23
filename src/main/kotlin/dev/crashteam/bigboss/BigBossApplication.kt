package dev.crashteam.bigboss

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class BigBossApplication

fun main(args: Array<String>) {
    runApplication<BigBossApplication>(*args)
}
