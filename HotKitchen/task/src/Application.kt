package hotkitchen


import hotkitchen.db.setupDb
import hotkitchen.error.*
import hotkitchen.plugins.configureRouting
import hotkitchen.plugins.configureValidation
import hotkitchen.plugins.setupJwt

import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module(testing: Boolean = false) {

    setupDb()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    setupJwt()

    configureValidation()

    configureStatusPages()

    configureRouting()
}