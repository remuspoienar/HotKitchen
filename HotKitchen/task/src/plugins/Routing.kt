package hotkitchen.plugins

import hotkitchen.db.*
import hotkitchen.error.*
import hotkitchen.routes.routeCategories
import hotkitchen.routes.routeMeals
import hotkitchen.routes.routeOrders
import hotkitchen.routes.routeUserAuth
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Application.configureRouting() {
    routing {
        routeUserAuth()

        routeCategories()

        routeMeals()

        routeOrders()
    }
}

fun PipelineContext<Unit, ApplicationCall>.currentUserId(): Int =
    call.principal<JWTPrincipal>()!!.payload.getClaim("id").asInt()

fun PipelineContext<Unit, ApplicationCall>.authorize(requiredUserType: String, user: User): Unit {
    if (user.userType != requiredUserType) throw MissingPermissionError(requiredUserType)
}
