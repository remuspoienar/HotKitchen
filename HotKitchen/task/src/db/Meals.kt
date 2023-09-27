package hotkitchen.db

import hotkitchen.error.CategoryNotFound
import hotkitchen.error.InvalidFieldError
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SizedCollection

object Meals : IntIdTable() {
    val title = varchar("title", 100)
    val price = float("price")
    val imageUrl = varchar("imageUrl", 100)
}

class Meal(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Meal>(Meals) {
        fun create(payload: MealPayload): Meal {
            return Connection.trx {
                val ids = payload.categoryIds?.toList() ?: throw CategoryNotFound("id")
                val categoryList = Category.find { Categories.id inList ids }.toList()

                val meal = Meal.new {
                    title = payload.title
                    price = payload.price
                    imageUrl = payload.imageUrl
                    categories = SizedCollection(categoryList)
                }

                meal
            }
        }
    }

    var title by Meals.title
    var price by Meals.price
    var imageUrl by Meals.imageUrl

    var categories by Category via MealsCategories

    fun toSerializable() = MealPayload(id.value, title, price, imageUrl, categories.toList().map { it.id.value })
}

@Serializable
data class MealPayload(
    val mealId: Int,
    val title: String = "",
    val price: Float = -1f,
    val imageUrl: String = "",
    val categoryIds: List<Int>?
) {
    fun validate() {
        val missing = mapOf("title" to title, "imageUrl" to imageUrl).filter { it.value.isEmpty() }.keys.firstOrNull()
        if (missing != null) {
            throw InvalidFieldError(missing)
        }

        if (price == -1f) {
            throw InvalidFieldError("price")
        }

        if (categoryIds?.isEmpty() ?: true) {
            throw InvalidFieldError("categoryIds")
        }
    }

}