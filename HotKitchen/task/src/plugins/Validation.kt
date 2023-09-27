package hotkitchen.plugins

import hotkitchen.db.CategoryPayload
import hotkitchen.db.MealPayload
import hotkitchen.db.OrderPayload
import hotkitchen.db.UserPayload
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureValidation() {
    install(RequestValidation) {
        validate<UserPayload> { user ->
            try {
                user.validate()
                ValidationResult.Valid
            } catch (e: Exception) {
                ValidationResult.Invalid(e.message!!)
            }
        }

        validate<CategoryPayload> {
            try {
                it.validate()
                ValidationResult.Valid
            } catch (e: Exception) {
                ValidationResult.Invalid(e.message!!)
            }
        }

        validate<MealPayload> {
            try {
                it.validate()
                ValidationResult.Valid
            } catch (e: Exception) {
                ValidationResult.Invalid(e.message!!)
            }
        }

        validate<OrderPayload> {
            try {
                it.validate()
                ValidationResult.Valid
            } catch (e: Exception) {
                ValidationResult.Invalid(e.message!!)
            }
        }
    }
}