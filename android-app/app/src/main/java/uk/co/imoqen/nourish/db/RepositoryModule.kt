package uk.co.imoqen.nourish.db

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uk.co.imoqen.nourish.db.ingredient.IngredientRepository
import uk.co.imoqen.nourish.db.ingredient.IngredientRepositoryImpl
import uk.co.imoqen.nourish.db.recipe.RecipeRepository
import uk.co.imoqen.nourish.db.recipe.RecipeRepositoryImpl
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepository
import uk.co.imoqen.nourish.db.shoppinglistentry.ShoppingListEntryRepositoryImpl
import uk.co.imoqen.nourish.db.tag.TagRepository
import uk.co.imoqen.nourish.db.tag.TagRepositoryImpl
import uk.co.imoqen.nourish.db.user.UserRepository
import uk.co.imoqen.nourish.db.user.UserRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // binds as the objects have concrete implementations, just need to map interface to implementation.
    @Binds
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindRecipeRepository(
        impl: RecipeRepositoryImpl
    ): RecipeRepository

    @Binds
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository

    @Binds
    abstract fun bindIngredientRepository(
        impl: IngredientRepositoryImpl
    ): IngredientRepository

    @Binds
    abstract fun bindShoppingListEntryRepository(
        impl: ShoppingListEntryRepositoryImpl
    ): ShoppingListEntryRepository
}