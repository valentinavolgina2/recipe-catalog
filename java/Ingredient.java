//CSD 230 Final Project - Valentina Volgina
package edu.lwtech.finalp;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "ingredients",
        foreignKeys = @ForeignKey(entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipe"))
public class Ingredient {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;

    @ColumnInfo(name = "ingredient")
    private String mIngredient;

    @ColumnInfo(name = "amountUS")
    private String mAmountUS;

    @ColumnInfo(name = "amountMetric")
    private String mAmountMetric;

    @ColumnInfo(name = "unitUS")
    private String mUnitUS;

    @ColumnInfo(name = "unitMetric")
    private String mUnitMetric;

    @ForeignKey(entity = Recipe.class, parentColumns = "id", childColumns = "recipe")
    @ColumnInfo(name = "recipe")
    private long mRecipe;

    public String getIngredient() {
        return mIngredient;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setIngredient(String text) {
        mIngredient = text;
    }

    public String getAmountUS() {
        return mAmountUS;
    }

    public void setAmountUS(String amount) {
        mAmountUS = amount;
    }

    public String getAmountMetric() {
        return mAmountMetric;
    }

    public void setAmountMetric(String amount) {
        mAmountMetric = amount;
    }

    public String getUnitUS() {
        return mUnitUS;
    }

    public void setUnitUS(String unitUS) {
        mUnitUS = unitUS;
    }

    public String getUnitMetric() {
        return mUnitMetric;
    }

    public void setUnitMetric(String unitMetric) {
        mUnitMetric = unitMetric;
    }

    public long getRecipe() {
        return mRecipe;
    }

    public void setRecipe(long recipeId) {
        mRecipe = recipeId;
    }

    public boolean isEqual(Ingredient other){
        return (this.mIngredient.equals(other.mIngredient) &&
                this.mAmountUS.equals(other.mAmountUS) &&
                this.mAmountMetric.equals(other.mAmountMetric) &&
                this.mUnitUS.equals(other.mUnitUS) &&
                this.mUnitMetric.equals(other.mUnitMetric) &&
                this.mRecipe==other.mRecipe);
    }


}
