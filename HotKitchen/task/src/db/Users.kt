package hotkitchen.db

import hotkitchen.error.InvalidFieldError
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.apache.commons.validator.routines.EmailValidator

@Serializable
data class UserPayload(
    val email: String,
    val password: String,
    val userType: String,
    val name: String = "",
    val phone: String = "",
    val address: String = ""
) {

    @Transient
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"

    @Transient
    private val passRegex = Regex("^(?=.*?\\d)(?=.*?[a-zA-Z])[a-zA-Z\\d]+\$")

    fun validate() {
        val missing = mapOf("name" to name, "phone" to phone, "address" to address).filter { it.value.isEmpty() }.keys.firstOrNull()
        if(missing != null) {
            throw InvalidFieldError(missing)
        }

        if (!email.matches(emailRegex.toRegex()) || !EmailValidator.getInstance().isValid(email)) {
            throw InvalidFieldError("email")
        }

        if (!password.matches(passRegex) || password.length < 6) {
            throw InvalidFieldError("password")
        }

    }
}

@Serializable
data class UserLogin(val email: String, val password: String)

@Serializable
data class User(
    @Transient val id: Int = -1,
    @Transient val password: String = "",
    val name: String,
    val userType: String,
    val phone: String,
    val email: String,
    val address: String
)

object Users : IntIdTable() {
    val email: Column<String> = varchar("email", 100).uniqueIndex()
    val password: Column<String> = varchar("password", 100)
    val userType: Column<String> = varchar("userType", 100)
    val name: Column<String> = varchar("name", 100).default("Goose")
    val phone: Column<String> = varchar("phone", 100).default("+79999999999")
    val address: Column<String> = varchar("address", 100).default("address")

    // fallback that acts as migrating existing rows before the new columns were added
    init {
        update(where = { IsNullOp(phone) }, body = { it[phone] = "+79999999999" })
        update(where = { IsNullOp(address) }, body = { it[address] = "address" })
        update(where = { IsNullOp(name) }, body = { it[name] = "Goose" })
    }

}