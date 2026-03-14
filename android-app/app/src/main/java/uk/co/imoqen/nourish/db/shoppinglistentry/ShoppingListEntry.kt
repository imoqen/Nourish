package uk.co.imoqen.nourish.db.shoppinglistentry

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import uk.co.imoqen.nourish.db.ingredient.Ingredient
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit
import uk.co.imoqen.nourish.db.user.User

@Entity(
    tableName = "ShoppingListEntry",
    foreignKeys = [
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"], // the primary key of Recipe
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE // if the ingredient gets deleted, shopping list entry gets deleted. this wont be a problem, because ingredients never get deleted (unless the app gets deleted)
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"], // the primary key of Recipe
            childColumns = ["purchaserUid"],
            onDelete = ForeignKey.CASCADE // if the user gets deleted, shopping list entry gets deleted.
        )
    ],
    indices = [
        Index("ingredientId"),
        Index("purchaserUid")
    ]
)
data class ShoppingListEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "ingredientId") val ingredientId: Long, // foreign key for ingredient relation
    @ColumnInfo(name = "purchaserUid") val purchaserUid: String, // foreign key for user relation
    @ColumnInfo(name = "unit") val unit: IngredientUnit, // optional, not all users want to enter a unit
    @ColumnInfo(name = "amount") val amount: Double, // optional, not all users want to enter an amount
    @ColumnInfo(name = "purchased") val purchased: Boolean,
    @ColumnInfo(name = "ordinal") val ordinal: Int // will get incremented from the current max ordinal when adding, so this represents a position in the list.
)