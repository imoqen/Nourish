package uk.co.imoqen.nourish.db

import androidx.room.TypeConverter
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

/**
 * Converter class, used to convert a String to an IngredientUnit and conversely.
 * This is required because the Room database can only store basic types, however IngredientUnits are
 * required.
 */
class Converters {

    @TypeConverter
    fun stringFromIngredientUnit(unit: IngredientUnit?): String? {
        return unit?.name
    }

    @TypeConverter
    fun stringToIngredientUnit(string: String): IngredientUnit? {
        try {
            return IngredientUnit.valueOf(string)
        }
        catch (e: Exception) {
            return null
        }
    }
}