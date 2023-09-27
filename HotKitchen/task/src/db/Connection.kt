package hotkitchen.db

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Connection {
    private val db = Database.connect("jdbc:postgresql://localhost:5432/hotkitchen", driver = "org.postgresql.Driver", user = "postgres", password = "postgres")
    fun setup() {
        transaction(db) {
            addLogger(StdOutSqlLogger)

            SchemaUtils.createMissingTablesAndColumns(Users)
            SchemaUtils.create(Users, Categories, Meals, MealsCategories, Orders, MealsOrders)
        }
    }

    fun <T> trx(blk: Transaction.() -> T): T =
        transaction(db, statement = blk)
}

fun Application.setupDb() {
    Connection.setup()
}
