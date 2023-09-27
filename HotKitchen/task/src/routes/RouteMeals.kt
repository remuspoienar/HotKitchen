package hotkitchen.routes

import hotkitchen.dao.userRepository
import hotkitchen.db.Connection
import hotkitchen.db.Meal
import hotkitchen.db.MealPayload
import hotkitchen.error.MealNotFound
import hotkitchen.plugins.authorize
import hotkitchen.plugins.currentUserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.load

fun Route.routeMeals() {

    authenticate("auth-jwt") {

        post("/meals") {
            val id = currentUserId()
            val user = userRepository.getUser(id)
            authorize("staff", user)

            val payload = call.receive<MealPayload>()

            val serializedMeal = Connection.trx {
                val meal = Meal.create(payload)
                meal.load(Meal::categories).toSerializable()
            }

            call.respond(serializedMeal)
        }


        get("/meals") {
            val result = Connection.trx {
                val id = call.request.queryParameters["id"]?.toInt()
                if (id == null) {
                    Meal.all().map(Meal::toSerializable)
                } else {
                    Meal.findById(id)?.load(Meal::categories)?.toSerializable() ?: throw MealNotFound("id = $id")
                }
            }

            call.respond(result)
        }
    }

}