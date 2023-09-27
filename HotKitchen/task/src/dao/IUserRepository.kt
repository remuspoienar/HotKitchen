package hotkitchen.dao

import hotkitchen.db.User
import hotkitchen.db.UserPayload
import org.jetbrains.exposed.dao.id.EntityID

interface IUserRepository {
    fun getUser(id: Int): User
    fun authenticateUser(email: String, password: String): User
    fun findUserFromJwt(id: Int?): EntityID<Int>?
    fun insertUser(userInput: UserPayload): User
    fun updateUser(id: Int, userInput: UserPayload): User
    fun deleteUser(id: Int): Unit
}