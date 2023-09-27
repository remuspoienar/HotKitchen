package hotkitchen.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import hotkitchen.dao.userRepository
import hotkitchen.db.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import java.util.*

fun Application.setupJwt() {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {

        jwt("auth-jwt") {
            realm = myRealm
            validate { jwtCredential ->
                val result = userRepository.findUserFromJwt(jwtCredential.payload.getClaim("id").asInt())
                if (result != null) {
                    JWTPrincipal(jwtCredential.payload)
                } else {
                    null
                }
            }

            challenge { defaultScheme, realm ->
                if (call.request.uri.endsWith("/me")) call.respond(HttpStatusCode.BadRequest)
                else call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }

            verifier(
                JWT.require(Algorithm.HMAC256(secret)).withAudience(audience).withIssuer(issuer).build()
            )
        }
    }
}

fun PipelineContext<Unit, ApplicationCall>.generateToken(user: User, call: ApplicationCall): String {
    val config = call.application.environment.config
    val secret = config.property("jwt.secret").getString()
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()

    return JWT.create().withAudience(audience).withIssuer(issuer).withClaim("id", user.id)
        .withExpiresAt(Date(System.currentTimeMillis() + 1_000 * 60 * 60 * 24)).sign(Algorithm.HMAC256(secret))
}