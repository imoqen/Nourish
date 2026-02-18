package uk.co.imoqen.nourish.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        NourishDatabase::class.java,
        "nourish_database"
    ).build()

    // Getting DAOs:

    @Singleton
    @Provides
    fun provideRecipeDao(db: NourishDatabase) = db.recipeDao()

    @Singleton
    @Provides
    fun provideUserDao(db: NourishDatabase) = db.userDao()

    @Singleton
    @Provides
    fun provideIngredientDao(db: NourishDatabase) = db.ingredientDao()
}