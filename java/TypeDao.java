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
public interface TypeDao {

    @Query("SELECT * FROM types WHERE text = :text")
    public Type getType(String text);

    @Query("SELECT * FROM types ORDER BY text")
    public List<Type> getTypes();

    @Query("SELECT * FROM types ORDER BY updated DESC")
    public List<Type> getTypesNewerFirst();

    @Query("SELECT * FROM types ORDER BY updated ASC")
    public List<Type> getTypesOlderFirst();

    @Insert(onConflict = OnConflictStrategy.FAIL)
    public void insertType(Type type);

    @Update
    public void updateType(Type type);

    @Delete
    public void deleteType(Type type);

}
