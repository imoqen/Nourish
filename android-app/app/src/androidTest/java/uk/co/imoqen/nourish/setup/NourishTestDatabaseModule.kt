package uk.co.imoqen.nourish.setup

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module // maybe this should be androidx test instead? if issues occur, change. as i noticed there were 2 when choosing.
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import uk.co.imoqen.nourish.db.NourishDatabase
import uk.co.imoqen.nourish.db.NourishDatabaseModule
import javax.inject.Named
import javax.inject.Singleton

// Creating an in-memory Room database to replace the production
// Nourish database, using a technique adapted from https://medium.com/@mojtabashirkhani.nba/testing-room-with-hilt-in-android-a-comprehensive-guide-98e2e25acdc6

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NourishDatabaseModule::class]
)
object NourishTestDatabaseModule {

    @Provides
    @Singleton
    //@Named("test_database")
    fun provideInMemoryDb(@ApplicationContext context: Context): NourishDatabase =
        Room.inMemoryDatabaseBuilder(
            context, NourishDatabase::class.java
        )
            .addCallback(object: RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    // The seeding to add recipe types when DB first created:

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
            .allowMainThreadQueries()
            .build()

    // Injecting the DAOs into the in-memory db:
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