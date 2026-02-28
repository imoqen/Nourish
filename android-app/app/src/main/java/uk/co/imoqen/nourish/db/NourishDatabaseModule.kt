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
import uk.co.imoqen.nourish.db.tag.Tag
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
                        val tagDao = db.tagDao()

                        // add the recipe types:
                        recipeTypeDao.insert(RecipeType(title = "Breakfast", ordinal=0))
                        recipeTypeDao.insert(RecipeType(title = "Lunch", ordinal=1))
                        recipeTypeDao.insert(RecipeType(title = "Dinner", ordinal=2))
                        recipeTypeDao.insert(RecipeType(title = "Dessert", ordinal=3))
                        recipeTypeDao.insert(RecipeType(title = "Side", ordinal=4))
                        recipeTypeDao.insert(RecipeType(title = "Drink", ordinal=5))

                        // add the recipe tags:
                        // dietary requirements:
                        tagDao.insert(Tag(identifier = "VEGAN", displayName = "Vegan", colour = "GREEN"))
                        tagDao.insert(Tag(identifier = "VEGETARIAN", displayName = "Vegetarian", colour = "YELLOW"))
                        tagDao.insert(Tag(identifier = "GLUTEN-FREE", displayName = "Gluten-free", colour = "ORANGE"))
                        tagDao.insert(Tag(identifier = "NUT-FREE", displayName = "Nut-free", colour = "RED"))
                        tagDao.insert(Tag(identifier = "DAIRY-FREE", displayName = "Dairy-free", colour = "BLUE"))
                        tagDao.insert(Tag(identifier = "SOY-FREE", displayName = "Soy-free", colour = "PINK"))
                        tagDao.insert(Tag(identifier = "EGG-FREE", displayName = "Egg-free", colour = "ORANGE"))
                        tagDao.insert(Tag(identifier = "LACTOSE-FREE", displayName = "Lactose-free", colour = "GREY"))
                        tagDao.insert(Tag(identifier = "WHEAT-FREE", displayName = "Wheat-free", colour = "YELLOW"))

                        // misc:
                        tagDao.insert(Tag(identifier = "FREEZER-FRIENDLY", displayName = "Freezer-friendly", colour = "BLUE"))
                        tagDao.insert(Tag(identifier = "UPF-FREE", displayName = "UPF-free", colour = "GREEN"))

                        // diet / macros:
                        tagDao.insert(Tag(identifier = "HIGH-PROTEIN", displayName = "High protein", colour = "PURPLE"))
                        tagDao.insert(Tag(identifier = "LOW-FAT", displayName = "Low fat", colour = "GREEN"))
                        tagDao.insert(Tag(identifier = "LOW-CALORIE", displayName = "Low calorie", colour = "PINK"))
                        tagDao.insert(Tag(identifier = "RICH-IN-VITAMINS-MINERALS", displayName = "Vitamins & Minerals", colour = "RED"))
                        tagDao.insert(Tag(identifier = "LOW-CARB", displayName = "Low carb", colour = "YELLOW"))
                        tagDao.insert(Tag(identifier = "HIGH-FIBRE", displayName = "High fibre", colour = "ORANGE"))
                        tagDao.insert(Tag(identifier = "NO-ADDED-SUGAR", displayName = "No added sugar", colour = "GREY"))

                    }
                }
            })
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

}