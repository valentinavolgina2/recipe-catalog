//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Ingredient.class, Recipe.class, Type.class}, version = 1)
public abstract class RecipeDatabase extends RoomDatabase{

    private static final String DATABASE_NAME = "recipe.db";

    private static RecipeDatabase mRecipeDatabase;

    // Singleton
    public static RecipeDatabase getInstance(Context context) {
        if (mRecipeDatabase == null) {
            mRecipeDatabase = Room.databaseBuilder(context, RecipeDatabase.class,
                    DATABASE_NAME).allowMainThreadQueries().build();
        }
        return mRecipeDatabase;
    }

    public abstract IngredientDao ingredientDao();
    public abstract RecipeDao recipeDao();
    public abstract TypeDao typeDao();


}
