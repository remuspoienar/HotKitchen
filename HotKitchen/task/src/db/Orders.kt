package hotkitchen.db


import hotkitchen.error.InvalidFieldError
import hotkitchen.error.MealNotFound
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.select

enum class OrderStatus(val status: String) {
    IN_PROGRESS("IN PROGRESS"),
    COMPLETE("COMPLETE")
}

object Orders : IntIdTable() {
    val userEmail = varchar("user_email", 100)
    val price = float("price")
    val address = varchar("address", 100)
    val status = enumeration<OrderStatus>("status")
}

class Order(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Order>(Orders) {
        fun create(payload: OrderPayload): Order {
            val ids = payload.mealIds?.toList() ?: throw MealNotFound("id: empty list")
            val mealList = Meal.find { Meals.id inList ids }.toList()
            if(mealList.isEmpty() || mealList.size != ids.size) throw MealNotFound("id: some meals were not found")

            return Connection.trx {
                Order.new {
                    userEmail = payload.userEmail
                    price = payload.price
                    address = payload.address
                    status = OrderStatus.IN_PROGRESS
                    meals = SizedCollection(mealList)
                }
            }
        }
    }

    var userEmail by Orders.userEmail
    var price by Orders.price
    var address by Orders.address
    var status by Orders.status

    var meals by Meal via MealsOrders

    fun toSerializable() = OrderPayload(
        id.value,
        userEmail,
        price,
        address,
        status.name,
        meals.toList().map { it.id.value })
}

@Serializable
data class OrderPayload(
    val orderId: Int = 0,
    val userEmail: String = "",
    val price: Float = -1f,
    val address: String = "",
    val status: String = "",
    val mealIds: List<Int>?
) {
    fun validate() {
        val missing =
            mapOf("userEmail" to userEmail, "address" to address).filter { it.value.isEmpty() }.keys.firstOrNull()
        if (missing != null) {
            throw InvalidFieldError(missing)
        }

        val userMatched = Connection.trx { Users.select { Users.email eq userEmail }.count() }
        if(userMatched.toInt() == 0) {
            throw InvalidFieldError("email: No user has such email")
        }

        if(status.isNotEmpty() && status == OrderStatus.COMPLETE.name) {
            throw InvalidFieldError("status: can only be IN PROGRESS")
        }

        if(mealIds?.isEmpty() == true) {
            throw InvalidFieldError("mealIds")
        }
    }
}