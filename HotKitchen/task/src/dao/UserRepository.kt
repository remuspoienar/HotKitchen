package hotkitchen.dao

import hotkitchen.db.Connection
import hotkitchen.db.User
import hotkitchen.db.UserPayload
import hotkitchen.db.Users
import hotkitchen.error.UserNotFound
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.lang.Exception

class UserRepository : IUserRepository {
    private fun resultRowToUser(row: ResultRow) = User(
        id = row[Users.id].value,
        email = row[Users.email],
        password = row[Users.password],
        userType = row[Users.userType],
        phone = row[Users.phone],
        address = row[Users.address],
        name = row[Users.name]
    )

    override fun insertUser(userInput: UserPayload): User {
        return Connection.trx {
            Users.insert {
                it[email] = userInput.email
                it[password] = userInput.password
                it[userType] = userInput.userType
                it[name] = userInput.name
                it[phone] = userInput.phone
                it[address] = userInput.address

            }.resultedValues!!.first().let(::resultRowToUser)
        }
    }

    override fun getUser(id: Int): User {
        return Connection.trx<User> {
            Users.select { Users.id eq id }.map(::resultRowToUser).singleOrNull() ?: throw UserNotFound("id = $id")
        }
    }

    override fun authenticateUser(email: String, password: String): User {
        return Connection.trx<User> {
            Users.select { (Users.email eq email) and (Users.password eq password) }.map(::resultRowToUser)
                .singleOrNull() ?: throw Exception("User $id not found")
        }
    }

    override fun findUserFromJwt(id: Int?): EntityID<Int>? {
        if (id == null) return null
        return Connection.trx {
            Users.select { Users.id eq id }.firstOrNull()?.get(Users.id) ?: throw Exception("User $id not found")
        }
    }

    override fun updateUser(id: Int, userInput: UserPayload): User {
        Connection.trx {
            Users.update(where = { Users.id eq id }, body = {
                it[userType] = userInput.userType
                it[name] = userInput.name
                it[phone] = userInput.phone
                it[address] = userInput.address
            })
        }
        return this.getUser(id)
    }

    override fun deleteUser(id: Int) {
        Connection.trx {
            Users.deleteWhere { Users.id eq id }
        }
    }
}

val userRepository: IUserRepository = UserRepository()