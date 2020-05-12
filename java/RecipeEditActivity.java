//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.TableDataLongClickListener;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;


public class RecipeEditActivity extends AppCompatActivity{

    public static final String EXTRA_RECIPE_ID = "edu.lwtech.finalproject.recipe_id";
    public static final String EXTRA_TYPE = "edu.lwtech.finalproject.type";

    private final int REQUEST_CODE_NEW_INGREDIENT = 0;
    private final int REQUEST_CODE_UPDATE_INGREDIENT = 1;

    private EditText mRecipeText;
    private EditText mDescriptionText;

    private RecipeDatabase mRecipeDb;
    private long mRecipeId;
    private long mIngredientId;
    private Recipe mRecipe;

    String[] ingredientHeaders={" ","Ingredient","Amount","Unit"};
    String[][] ingredients;
    private List<Ingredient> mIngredients;
    private List<Ingredient> mOldIngredients;
    TableView<String[]> mTb;

    private int mMetricSystem;
    private SharedPreferences mSharedPrefs;

    boolean newRecipeSaved = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_edit);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mMetricSystem = Integer.parseInt(mSharedPrefs.getString(SettingsFragment.PREFERENCE_UNIT, "0"));


        mRecipeText = findViewById(R.id.questionText);
        mDescriptionText = findViewById(R.id.answerText);

        mRecipeDb = RecipeDatabase.getInstance(getApplicationContext());

        // Get question ID from QuestionActivity
        Intent intent = getIntent();
        mRecipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1);

        ActionBar actionBar = getSupportActionBar();

        mIngredientId = -1;

        if (mRecipeId == -1) {
            // Add new question
            mRecipe = new Recipe();
            setTitle(R.string.add_recipe);

        } else {
            // Update existing question
            mRecipe = mRecipeDb.recipeDao().getRecipe(mRecipeId);
            mRecipeText.setText(mRecipe.getText());
            mDescriptionText.setText(mRecipe.getDescription());
            setTitle(R.string.update_recipe);
        }

        String type = intent.getStringExtra(EXTRA_TYPE);
        mRecipe.setType(type);

        mTb = (TableView<String[]>)findViewById(R.id.tableView);
        mTb.setColumnCount(4);

        mTb.setColumnWeight(0,0);
        mTb.setColumnWeight(1,4);
        mTb.setColumnWeight(2,3);
        mTb.setColumnWeight(3,2);

        mOldIngredients = mRecipeDb.ingredientDao().getIngredients(mRecipeId);
        showIngredients();

        mTb.setHeaderAdapter(new SimpleTableHeaderAdapter(this,ingredientHeaders));

        mTb.addDataLongClickListener(new TableDataLongClickListener<String[]>() {
            @Override
            public boolean onDataLongClicked(int rowIndex, String[] clickedData) {
                mIngredientId = Long.parseLong(clickedData[0]);
                return false;
            }
        });


        registerForContextMenu(mTb);

    }

    private void showIngredients(){

        mIngredients = mRecipeDb.ingredientDao().getIngredients(mRecipeId);
        ingredients = new String[mIngredients.size()][4];

        for(int i = 0; i < mIngredients.size(); i++){
            Ingredient ing = mIngredients.get(i);
            ingredients[i][0] = String.valueOf(ing.getId());
            ingredients[i][1] = ing.getIngredient();


            if(mMetricSystem==0) {
                ingredients[i][2] = ing.getAmountUS();
                ingredients[i][3] = ing.getUnitUS();
            }
            else {
                ingredients[i][2] = ing.getAmountMetric();
                ingredients[i][3] = ing.getUnitMetric();
            }
        }
        mTb.setDataAdapter(new SimpleTableDataAdapter(this,ingredients));

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                editIngredient();
                return true;
            case R.id.delete:
                deleteIngredient();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate menu for the app bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_cancel_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Determine which app bar item was chosen
        switch (item.getItemId()) {
            case R.id.cancel:
                cancelClick();
                return true;
            case R.id.save:
                saveRecipe();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void editIngredient(){

        Intent intent = new Intent(this, IngredientEditActivity.class);
        intent.putExtra(IngredientEditActivity.EXTRA_RECIPE_ID, mRecipeId);
        intent.putExtra(IngredientEditActivity.EXTRA_INGREDIENT_ID, mIngredientId);
        startActivityForResult(intent, REQUEST_CODE_NEW_INGREDIENT);
    }

    private void deleteIngredient(){
        mRecipeDb.ingredientDao().deleteIngredient(mRecipeDb.ingredientDao().getIngredient(mIngredientId));
        Toast.makeText(this, R.string.ingredient_deleted, Toast.LENGTH_SHORT).show();
        showIngredients();
    }

    private void cancelClick(){

        //cancel all changes in ingredients table
        for(int i = 0; i < mIngredients.size(); i++){

            Ingredient newIngredient = mIngredients.get(i);
            int index = findInOldList(newIngredient);
            if(index == -1)
                deleteIngredient(newIngredient);
            else{
                if(newIngredient.isEqual(mOldIngredients.get(index)))
                    mOldIngredients.remove(index);
                else
                    updateIngredient(index);
            }
            insertRemainIngredients();
        }

        //delete the recipe if it has been just created
        if(newRecipeSaved){
            Recipe recipe = mRecipeDb.recipeDao().getRecipe(mRecipeId);
            mRecipeDb.recipeDao().deleteRecipe(recipe);
        }


        // Send back question ID
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private int findInOldList(Ingredient ingredient){
        for(int i=0; i < mOldIngredients.size(); i++){
            if(mOldIngredients.get(i).getId() == ingredient.getId()) return i;
        }
        return -1;
    }

    private void updateIngredient(int i){
        mRecipeDb.ingredientDao().updateIngredient(mOldIngredients.get(i));
        mOldIngredients.remove(i);
    }

    private void insertRemainIngredients(){
        for(int i = 0; i < mOldIngredients.size(); i++){
            mRecipeDb.ingredientDao().insertIngredient(mOldIngredients.get(i));
        }
    }

    private void deleteIngredient(Ingredient ingredient){
        mRecipeDb.ingredientDao().deleteIngredient(ingredient);
    }


    public void saveRecipe(){
        mRecipe.setText(mRecipeText.getText().toString());
        mRecipe.setDescription(mDescriptionText.getText().toString());

        if (mRecipeId == -1) {
            // New question
            mRecipeId = mRecipeDb.recipeDao().insertRecipe(mRecipe);
        } else {
            // Existing question
            mRecipeDb.recipeDao().updateRecipe(mRecipe);
        }

        // Send back question ID
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RECIPE_ID, mRecipeId);
        setResult(RESULT_OK, intent);
        finish();

    }


    public void addIngredientButtonClick(View view) {

        if(mRecipeId==-1){
            mRecipe.setText(mRecipeText.getText().toString());
            mRecipe.setDescription(mDescriptionText.getText().toString());
            mRecipeId = mRecipeDb.recipeDao().insertRecipe(mRecipe);
            newRecipeSaved = true;
        }

        Intent intent = new Intent(this, IngredientEditActivity.class);
        intent.putExtra(IngredientEditActivity.EXTRA_RECIPE_ID, mRecipeId);
        startActivityForResult(intent, REQUEST_CODE_NEW_INGREDIENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_NEW_INGREDIENT) {
            showIngredients();
            Toast.makeText(this, R.string.ingredient_added, Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_INGREDIENT) {
            showIngredients();
            Toast.makeText(this, R.string.ingredient_updated, Toast.LENGTH_SHORT).show();
        }



    }


}
