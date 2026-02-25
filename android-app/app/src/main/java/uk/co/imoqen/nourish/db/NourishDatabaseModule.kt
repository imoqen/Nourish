package uk.co.imoqen.nourish.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.co.imoqen.nourish.db.recipe.recipetype.RecipeType
import javax.inject.Singleton

// Hilt dependency injection for Room database
// https://stackoverflow.com/questions/63146318/how-to-create-and-use-a-room-database-in-kotlin-dagger-hilt

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
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = dbProvider.get()
                        val recipeTypeDao = db.recipeTypeDao()

                        // add the recipe types:
                        recipeTypeDao.insert(RecipeType(title = "Breakfast"))
                        recipeTypeDao.insert(RecipeType(title = "Lunch"))
                        recipeTypeDao.insert(RecipeType(title = "Dinner"))
                        recipeTypeDao.insert(RecipeType(title = "Dessert"))
                        recipeTypeDao.insert(RecipeType(title = "Side"))
                        recipeTypeDao.insert(RecipeType(title = "Snack"))
                        recipeTypeDao.insert(RecipeType(title = "Drink"))
                    }
                }
            })
            .build()
    }


    // all DAOs provided here to Hilt:

    @Singleton
    @Provides
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

}