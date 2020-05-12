//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;


public class RecipeActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "edu.lwtech.finalproject.type";
    private final int REQUEST_CODE_NEW_RECIPE = 0;
    private final int REQUEST_CODE_UPDATE_RECIPE = 1;

    private Recipe mDeletedRecipe;

    private RecipeDatabase mRecipeDb;
    private String mType;
    private List<Recipe> mRecipes;
    private TextView mDescriptionText;
    private TextView mRecipeText;
    private int mCurrentRecipeIndex;
    private ViewGroup mShowRecipesLayout;
    private ViewGroup mNoRecipesLayout;

    String[] ingredientHeaders={"Ingredient","Amount","Unit"};
    String[][] ingredients;
    private List<Ingredient> mIngredients;
    TableView<String[]> mTb;

    private int mMetricSystem;
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mMetricSystem = Integer.parseInt(mSharedPrefs.getString(SettingsFragment.PREFERENCE_UNIT, "0"));


        // Hosting activity provides the subject of the questions to display
        Intent intent = getIntent();
        mType = intent.getStringExtra(EXTRA_TYPE);

        // Load all questions for this subject
        mRecipeDb = RecipeDatabase.getInstance(getApplicationContext());
        mRecipes = mRecipeDb.recipeDao().getRecipes(mType);

        mRecipeText = findViewById(R.id.questionText);
        mDescriptionText = findViewById(R.id.answerText);
        mShowRecipesLayout = findViewById(R.id.showQuestionsLayout);
        mNoRecipesLayout = findViewById(R.id.noQuestionsLayout);


        mTb = (TableView<String[]>)findViewById(R.id.tableView);
        mTb.setColumnCount(3);

        mTb.setColumnWeight(0,4);
        mTb.setColumnWeight(1,3);
        mTb.setColumnWeight(2,2);

        mTb.setHeaderAdapter(new SimpleTableHeaderAdapter(this,ingredientHeaders));

        // Show first question
        showRecipe(0);

    }

    private void showIngredients(){
        if(mCurrentRecipeIndex==-1)
            return;

        Recipe recipe = mRecipes.get(mCurrentRecipeIndex);
        long recipeId = recipe.getId();

        mIngredients = mRecipeDb.ingredientDao().getIngredients(recipeId);
        ingredients = new String[mIngredients.size()][3];

        for(int i = 0; i < mIngredients.size(); i++){
            Ingredient ing = mIngredients.get(i);
            ingredients[i][0] = ing.getIngredient();

            if(mMetricSystem==0) {
                ingredients[i][1] = ing.getAmountUS();
                ingredients[i][2] = ing.getUnitUS();
            }
            else {
                ingredients[i][1] = ing.getAmountMetric();
                ingredients[i][2] = ing.getUnitMetric();
            }
        }
        mTb.setDataAdapter(new SimpleTableDataAdapter(this,ingredients));

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Are there recipes to display?
        if (mRecipes.size() == 0) {
            updateAppBarTitle();
            displayRecipe(false);
        } else {
            displayRecipe(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate menu for the app bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Determine which app bar item was chosen
        switch (item.getItemId()) {
            case R.id.previous:
                showRecipe(mCurrentRecipeIndex - 1);
                return true;
            case R.id.next:
                showRecipe(mCurrentRecipeIndex + 1);
                return true;
            case R.id.add:
                addRecipe();
                return true;
            case R.id.edit:
                editRecipe();
                return true;
            case R.id.delete:
                deleteRecipe();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addRecipeButtonClick(View view) {
        addRecipe();
    }

    private void displayRecipe(boolean display) {

        // Show or hide the appropriate screen
        if (display) {
            mShowRecipesLayout.setVisibility(View.VISIBLE);
            mNoRecipesLayout.setVisibility(View.GONE);
        } else {
            mShowRecipesLayout.setVisibility(View.GONE);
            mNoRecipesLayout.setVisibility(View.VISIBLE);
        }
    }

    private void updateAppBarTitle() {

        // Display subject and number of questions in app bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = getResources().getString(R.string.recipe_number,
                    mType, mCurrentRecipeIndex + 1, mRecipes.size());
            setTitle(title);
        }
    }

    private void addRecipe() {
        Intent intent = new Intent(this, RecipeEditActivity.class);
        intent.putExtra(RecipeEditActivity.EXTRA_TYPE, mType);
        startActivityForResult(intent, REQUEST_CODE_NEW_RECIPE);
    }

    private void editRecipe() {
        if (mCurrentRecipeIndex >= 0) {
            Intent intent = new Intent(this, RecipeEditActivity.class);
            intent.putExtra(EXTRA_TYPE, mType);
            long recipeId = mRecipes.get(mCurrentRecipeIndex).getId();
            intent.putExtra(RecipeEditActivity.EXTRA_RECIPE_ID, recipeId);
            startActivityForResult(intent, REQUEST_CODE_UPDATE_RECIPE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_NEW_RECIPE) {
            // Get added recipe
            long recipeId = data.getLongExtra(RecipeEditActivity.EXTRA_RECIPE_ID, -1);
            Recipe newRecipe = mRecipeDb.recipeDao().getRecipe(recipeId);

            // Add newly created recipe to the recipe list and show it
            mRecipes.add(newRecipe);
            showRecipe(mRecipes.size() - 1);

            Toast.makeText(this, R.string.recipe_added, Toast.LENGTH_SHORT).show();
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_UPDATE_RECIPE) {
            // Get updated recipe
            long recipeId = data.getLongExtra(RecipeEditActivity.EXTRA_RECIPE_ID, -1);
            Recipe updatedRecipe = mRecipeDb.recipeDao().getRecipe(recipeId);

            // Replace current question in question list with updated question
            Recipe currentRecipe = mRecipes.get(mCurrentRecipeIndex);
            currentRecipe.setText(updatedRecipe.getText());
            currentRecipe.setDescription(updatedRecipe.getDescription());
            showRecipe(mCurrentRecipeIndex);

            Toast.makeText(this, R.string.recipe_updated, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRecipe() {
        if (mCurrentRecipeIndex >= 0) {

            Recipe recipe = mRecipes.get(mCurrentRecipeIndex);
            mDeletedRecipe = recipe; // Save question in case user undoes delete
            final List<Ingredient> deletedIngredients = mRecipeDb.ingredientDao().getIngredients(mDeletedRecipe.getId());

            deletedIngredients.addAll(mIngredients);

            //delete all the ingredients first
            for(Ingredient ingredient : mIngredients){
                mRecipeDb.ingredientDao().deleteIngredient(ingredient);
            }

            mRecipeDb.recipeDao().deleteRecipe(recipe);

            mRecipes.remove(mCurrentRecipeIndex);

            if (mRecipes.size() == 0) {
                // No questions to show
                mCurrentRecipeIndex = -1;
                updateAppBarTitle();
                displayRecipe(false);
            } else {
                showRecipe(mCurrentRecipeIndex);
            }

            // Show delete message with Undo button
            Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout),
                    R.string.recipe_deleted, Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add question back
                    mRecipeDb.recipeDao().insertRecipe(mDeletedRecipe);
                    mRecipes.add(mDeletedRecipe);

                    //add all the ingredients back
                    for(Ingredient ingredient : deletedIngredients){
                        mRecipeDb.ingredientDao().insertIngredient(ingredient);
                    }

                    showRecipe(mRecipes.size() - 1);
                    displayRecipe(true);
                }
            });
            snackbar.show();
        }
    }

    private void showRecipe(int recipeIndex) {

        // Show question at the given index
        if (mRecipes.size() > 0) {
            if (recipeIndex < 0) {
                recipeIndex = mRecipes.size() - 1;
            } else if (recipeIndex >= mRecipes.size()) {
                recipeIndex = 0;
            }

            mCurrentRecipeIndex = recipeIndex;
            updateAppBarTitle();

            Recipe recipe = mRecipes.get(mCurrentRecipeIndex);
            mRecipeText.setText(recipe.getText());
            mDescriptionText.setText(recipe.getDescription());
        }
        else {
            // No questions yet
            mCurrentRecipeIndex = -1;
        }

        showIngredients();
    }


}
