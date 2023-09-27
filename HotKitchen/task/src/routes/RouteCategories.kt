package hotkitchen.routes

import hotkitchen.dao.userRepository
import hotkitchen.db.Category
import hotkitchen.db.CategoryPayload
import hotkitchen.db.Connection
import hotkitchen.error.CategoryNotFound
import hotkitchen.plugins.authorize
import hotkitchen.plugins.currentUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.routeCategories() {
    authenticate("auth-jwt") {

        post("/categories") {
            val id = currentUserId()
            val user = userRepository.getUser(id)
            authorize("staff", user)

            val payload = call.receive<CategoryPayload>()

            val category = Category.create(payload)

            call.respond(category.toSerializable())
        }


        get("/categories") {
            val result = Connection.trx {
                val id = call.request.queryParameters["id"]?.toInt()
                if (id == null) {
                    Category.all().map(Category::toSerializable)
                } else {
                    Category.findById(id)?.toSerializable() ?: throw CategoryNotFound("id = $id")
                }
            }

            call.respond(result)
        }
    }
}