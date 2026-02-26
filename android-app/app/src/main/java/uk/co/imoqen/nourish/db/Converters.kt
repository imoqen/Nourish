package uk.co.imoqen.nourish.db

import android.util.Log
import androidx.room.TypeConverter
import uk.co.imoqen.nourish.db.ingredientunit.IngredientUnit

// to use enums: https://developer.android.com/training/data-storage/room/referencing-data
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
            Log.e("FORMAT ERROR", "$e")
            return null
        }
    }
}