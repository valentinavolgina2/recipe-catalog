//CSD 230 Final Project - Valentina Volgina
package edu.lwtech.finalp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE id = :id")
    public Recipe getRecipe(long id);

    @Query("SELECT * FROM recipes WHERE type = :type")
    public List<Recipe> getRecipes(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertRecipe(Recipe recipe);

    @Update
    public void updateRecipe(Recipe recipe);

    @Delete
    public void deleteRecipe(Recipe recipe);

}
