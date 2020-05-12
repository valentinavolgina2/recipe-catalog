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
public interface IngredientDao {

    @Query("SELECT * FROM ingredients WHERE id = :id")
    public Ingredient getIngredient(long id);

    @Query("SELECT * FROM ingredients WHERE recipe = :recipe")
    public List<Ingredient> getIngredients(long recipe);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertIngredient(Ingredient ingredient);

    @Update
    public void updateIngredient(Ingredient ingredient);

    @Delete
    public void deleteIngredient(Ingredient ingredient);

}
