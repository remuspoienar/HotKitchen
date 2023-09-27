package hotkitchen.db

import hotkitchen.error.InvalidFieldError
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Categories : IntIdTable() {
    val title = varchar("title", 100)
    val description = varchar("description", 100)
}

class Category(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Category>(Categories) {
        fun create(payload: CategoryPayload): Category {
            return Connection.trx {
                Category.new {
                    description = payload.description
                    title = payload.title
                }
            }
        }
    }

    var title by Categories.title
    var description by Categories.description

    fun toSerializable() = CategoryPayload(id.value, title, description)
}

@Serializable
data class CategoryPayload(
    val categoryId: Int = 0,
    val title: String = "",
    val description: String = "",
) {
    fun validate() {
        val missing =
            mapOf("description" to description, "title" to title).filter { it.value.isEmpty() }.keys.firstOrNull()
        if (missing != null) {
            throw InvalidFieldError(missing)
        }
    }
}