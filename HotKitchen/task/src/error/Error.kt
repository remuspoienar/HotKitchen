package hotkitchen.error

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException

open class InvalidFieldError(field: String) : Exception("""{"status":"Invalid $field"}""")
class UserAlreadyExists : Exception("""{"status":"User already exists"}""")
class UserNotFound(criteria: String) : Exception("""{"status":"Could not find user for $criteria"}""")
class CategoryNotFound(criteria: String) : Exception("""{"status":"Could not find category for $criteria"}""")
class MealNotFound(criteria: String) : Exception("""{"status":"Could not find meal for $criteria"}""")
class OrderNotFound(criteria: String) : Exception("""{"status":"Could not find order for $criteria"}""")
class EmailUpdateAttemptError : InvalidFieldError("email: Update not allowed")
class MissingPermissionError(requiredUserType: String) : Exception("""{"status":"User is not $requiredUserType"}""")

suspend fun StatusPagesConfig.jsonResponse(call: ApplicationCall, cause: Throwable, status: HttpStatusCode) =
    call.respondText(
        cause.message!!, ContentType.Application.Json, status
    )

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<Throwable> { call, cause ->
            when (cause) {

                is RequestValidationException -> call.respondText(
                    cause.reasons[0], ContentType.Application.Json, HttpStatusCode.Forbidden
                )

                is UserAlreadyExists -> call.respondText(
                    cause.message!!, ContentType.Application.Json, HttpStatusCode.Forbidden
                )

                is UserNotFound -> jsonResponse(call, cause, HttpStatusCode.NotFound)

                is CategoryNotFound -> jsonResponse(call, cause, HttpStatusCode.NotFound)

                is MealNotFound -> jsonResponse(call, cause, HttpStatusCode.BadRequest)

                is OrderNotFound -> jsonResponse(call, cause, HttpStatusCode.BadRequest)

                is EmailUpdateAttemptError -> jsonResponse(call, cause, HttpStatusCode.BadRequest)

                is MissingFieldException -> call.respondText(
                    cause.message ?: "Some fields are missing", ContentType.Application.Json, HttpStatusCode.Forbidden
                )

                is MissingPermissionError -> call.respond(
                    HttpStatusCode.Forbidden, hashMapOf("status" to "Access denied")
                )

                is BadRequestException -> call.respond(
                    HttpStatusCode.BadRequest, hashMapOf("status" to cause.message)
                )

                else -> call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            }

        }

    }
}
