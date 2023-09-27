package hotkitchen.routes

import hotkitchen.dao.userRepository
import hotkitchen.db.User
import hotkitchen.db.UserLogin
import hotkitchen.db.UserPayload
import hotkitchen.error.EmailUpdateAttemptError
import hotkitchen.error.UserAlreadyExists
import hotkitchen.error.UserNotFound
import hotkitchen.plugins.currentUserId
import hotkitchen.plugins.generateToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.routeUserAuth() {
    post("/signup") {
        val payload = call.receive<UserPayload>()

        try {
            val user = userRepository.insertUser(payload)
            val token = generateToken(user, call)

            call.response.header("Authorization", token)
            call.respond(hashMapOf("token" to token))
        } catch (e: Exception) {
            throw UserAlreadyExists()
        }
    }

    post("/signin") {
        val payload = call.receive<UserLogin>()
        val email = payload.email
        val password = payload.password

        try {
            val user = userRepository.authenticateUser(email, password)
            val token = generateToken(user, call)

            call.response.header("Authorization", token)
            call.respond(hashMapOf("token" to token))
        } catch (e: Exception) {
            call.respondText(
                """{"status":"Invalid email or password"}""", ContentType.Application.Json, HttpStatusCode.Forbidden
            )
        }

    }

    authenticate("auth-jwt") {
        get("/validate") {
            val id = currentUserId()
            val user = userRepository.getUser(id)
            call.respondText("Hello, ${user.userType} ${user.email}")
        }

        get("/me") {
            val id = currentUserId()
            val user = userRepository.getUser(id)
            call.respond(user)
        }

        put("/me") {
            val id = currentUserId()

            val payload = call.receive<UserPayload>()
            val result: User = try {
                val existingUser = userRepository.getUser(id)
                if (existingUser.email != payload.email) throw EmailUpdateAttemptError()
                userRepository.updateUser(id, payload)
            } catch (e: UserNotFound) {
                userRepository.insertUser(payload)
            }

            call.respond(result)
        }

        delete("/me") {
            val id = currentUserId()

            userRepository.getUser(id)
            userRepository.deleteUser(id)
        }
    }
}