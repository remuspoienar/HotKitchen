package hotkitchen.routes

import hotkitchen.dao.userRepository
import hotkitchen.db.*
import hotkitchen.error.InvalidFieldError
import hotkitchen.error.OrderNotFound
import hotkitchen.plugins.authorize
import hotkitchen.plugins.currentUserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.load

fun Route.routeOrders() {
    authenticate("auth-jwt") {

        route("/order") {

            post {

                println("matched this")
                val payload = call.receive<OrderPayload>()

                val serializedOrder = Connection.trx {
                    val order = Order.create(payload)
                    order.load(Order::meals).toSerializable()
                }

                call.respond(serializedOrder)
            }

            post("/{orderId}/markReady") {
                val userId = currentUserId()
                val user = userRepository.getUser(userId)
                authorize("staff", user)

                val id = call.parameters["orderId"]?.toInt() ?: throw OrderNotFound("id format")

                Connection.trx {
                    val order = Order.findById(id) ?: throw OrderNotFound("orderId")

                    order.status = OrderStatus.COMPLETE
                }

                call.respond(HttpStatusCode.OK)
            }

        }

        get("/orderHistory") {

            val records = Connection.trx {
                Order.all().map(Order::toSerializable)
            }

            call.respond(records)
        }

        get("/orderIncomplete") {

            val records = Connection.trx {
                Order.find { Orders.status eq OrderStatus.IN_PROGRESS }.map(Order::toSerializable)
            }

            call.respond(records)
        }


    }
}