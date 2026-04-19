package uk.co.imoqen.nourish.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection for the Room database (NourishDatabase)
 * https://stackoverflow.com/questions/63146318/how-to-create-and-use-a-room-database-in-kotlin-dagger-hilt
 *
 * @author Imogen Pemberton, 2026
 */

@Module
@InstallIn(SingletonComponent::class)
object NourishDatabaseModule {

    // Dependency injection of the DB:
    @Singleton
    @Provides
    fun provideNourishDatabase(
        @ApplicationContext app: Context,
        dbProvider: javax.inject.Provider<NourishDatabase>
    ): NourishDatabase {
        return Room.databaseBuilder(
            app,
            NourishDatabase::class.java,
            "nourish_database"
        )
            // Seed the database using a callback https://matthiaslischka.at/2019/01/15/seed-room-database/
            .addCallback(object: RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.i("ROOM_SEED", "Seeding the database")

                    // add the recipe types:
                    db.execSQL(
                        "INSERT OR IGNORE INTO RecipeType(title, ordinal) VALUES('Breakfast', 0)"
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO RecipeType(title, ordinal) VALUES('Lunch', 1)"
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO RecipeType(title, ordinal) VALUES('Dinner', 2)"
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO RecipeType(title, ordinal) VALUES('Dessert', 3)"
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO RecipeType(title, ordinal) VALUES('Side', 4)"
                    )
                    db.execSQL(
                        "INSERT OR IGNORE INTO RecipeType(title, ordinal) VALUES('Drink', 5)"
                    )
                }
            })
            .fallbackToDestructiveMigration(false)
            .build()
    }

    // all DAOs provided here to Hilt:

    @Singleton
    @Provides // provides as the object is constructed
    fun provideRecipeDao(db: NourishDatabase) = db.recipeDao()

    @Singleton
    @Provides
    fun provideRecipeIngredientDao(db: NourishDatabase) = db.recipeIngredientDao()

    @Singleton
    @Provides
    fun provideRecipeTypeDao(db: NourishDatabase) = db.recipeTypeDao()

    @Singleton
    @Provides
    fun provideUserDao(db: NourishDatabase) = db.userDao()

    @Singleton
    @Provides
    fun provideIngredientDao(db: NourishDatabase) = db.ingredientDao()

    @Singleton
    @Provides
    fun provideRecipeIngredientSectionDao(db: NourishDatabase) = db.recipeIngredientSectionDao()

    @Singleton
    @Provides
    fun provideRecipeTagDao(db: NourishDatabase) = db.recipeTagDao()

    @Singleton
    @Provides
    fun provideTagDao(db: NourishDatabase) = db.tagDao()

    @Singleton
    @Provides
    fun provideRecipeStepDao(db: NourishDatabase) = db.recipeStepDao()

    @Singleton
    @Provides
    fun provideRecipeNutritionDao(db: NourishDatabase) = db.recipeNutritionDao()

    @Singleton
    @Provides
    fun provideRecipeQueryDao(db: NourishDatabase) = db.recipeQueryDao()

    @Singleton
    @Provides
    fun provideShoppingListEntryDao(db: NourishDatabase) = db.shoppingListEntryDao()

}