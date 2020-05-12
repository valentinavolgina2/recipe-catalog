//CSD 230 Final Project - Valentina Volgina
package edu.lwtech.finalp;

import android.content.Intent;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class IngredientEditActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "edu.lwtech.finalproject.recipe_id";
    public static final String EXTRA_INGREDIENT_ID = "edu.lwtech.finalproject.ingredient_id";

    private EditText mIngredientText;
    private EditText mAmountText;

    private RecipeDatabase mRecipeDb;
    private long mRecipeId;
    private long mIngredientId;
    private Ingredient mIngredient;

    String mUnit = "";
    String[] mUSUnits;
    String[] mMetricUnits;
    private boolean mUS; // true - US, false - Metric
    private SharedPreferences mSharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_edit);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mUS = !Boolean.parseBoolean(mSharedPrefs.getString(SettingsFragment.PREFERENCE_UNIT, "0"));

        mIngredientText = findViewById(R.id.ingredientText);
        mAmountText = findViewById(R.id.amountText);

        mRecipeDb = RecipeDatabase.getInstance(getApplicationContext());


        // Get question ID from QuestionEditActivity
        Intent intent = getIntent();
        mRecipeId = intent.getLongExtra(EXTRA_RECIPE_ID, -1);
        mIngredientId = intent.getLongExtra(EXTRA_INGREDIENT_ID, -1);

        if (mIngredientId == -1) {
            // Add new ingredient
            mIngredient = new Ingredient();

            setTitle(R.string.add_ingredient);

        } else {

            // Update existing ingredient
            mIngredient = mRecipeDb.ingredientDao().getIngredient(mIngredientId);
            mIngredientText.setText(mIngredient.getIngredient());

            setTitle(R.string.update_ingredient);
        }

        mIngredient.setRecipe(mRecipeId);

        loadUnits();
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
                saveIngredient();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadUnits(){

        if (mIngredientId != -1){
            if(mUS){
                mAmountText.setText(mIngredient.getAmountUS());
                mUnit = mIngredient.getUnitUS();
            }else{
                mAmountText.setText(mIngredient.getAmountMetric());
                mUnit = mIngredient.getUnitMetric();
            }
        }



        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_unit);

        mUSUnits = getResources().getStringArray(R.array.us_units);
        mMetricUnits = getResources().getStringArray(R.array.metric_units);

        ArrayAdapter<String> adapter;
        if(mUS){
            adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, mUSUnits);
            setCurrentUnit(textView,mUSUnits);
        }else{
            adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, mMetricUnits);
            setCurrentUnit(textView,mMetricUnits);
        }

        textView.setThreshold(1);
        textView.setAdapter(adapter);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUnit = s.toString();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        changeUnitSwitch();

    }

    private void changeUnitSwitch(){

        TextView unitsInfoTV = (TextView)findViewById(R.id.unitsInfo);

        if(mUS){
            unitsInfoTV.setText("US units are turned on.");
        }else{
            unitsInfoTV.setText("Metric units are turned on.");
        }

    }

    public void switchUnitsButtonClick(View view) {
        mUS = !mUS;
        loadUnits();
    }

    private void setCurrentUnit(AutoCompleteTextView textView,String[] unitsArray){
        for(int i = 0; i < unitsArray.length; i++){
            if(unitsArray[i].equals(mUnit)){
                textView.setText(mUnit);
                return;
            }
        }
        textView.setText(mUnit);
    }

    private void saveIngredient(){
        mIngredient.setIngredient(mIngredientText.getText().toString());

        mUnit = formatUnit(mUnit);
        String currentAmout = mAmountText.getText().toString();

        if(mUS){
            mIngredient.setAmountUS(currentAmout);
            mIngredient.setUnitUS(mUnit);

            mIngredient.setAmountMetric(convertToMetric(currentAmout,mUnit));
            mIngredient.setUnitMetric(getMetricUnit(mUnit));

        }else{
            mIngredient.setAmountMetric(currentAmout);
            mIngredient.setUnitMetric(mUnit);

            mIngredient.setAmountUS(convertToUS(currentAmout,mUnit));
            mIngredient.setUnitUS(getUSUnit(mUnit));
        }


        if (mIngredientId == -1) {
            // New ingredient
            mRecipeDb.ingredientDao().insertIngredient(mIngredient);
        } else {
            // Existing ingredient
            mRecipeDb.ingredientDao().updateIngredient(mIngredient);
        }

        // Send back ingredient ID
        Intent intent = new Intent();
        intent.putExtra(EXTRA_INGREDIENT_ID, mIngredient.getId());

        setResult(RESULT_OK, intent);
        finish();

    }

    private void cancelClick(){
        // Send back ingredient ID
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }


    private String formatUnit(String unit){
        if(unit.isEmpty()) return unit;
        unit.trim();
        if(unit.equals("F") || unit.equals("C"))
            return unit;
        else if(unit.equals("f") || unit.equals("c"))
            return unit.toUpperCase();
        else
            return unit.toLowerCase();
    }

    private String convertToMetric(String amountStr, String unitUS){

        double amountUS = Double.parseDouble(amountStr);
        double amountMetric =  amountUS;

        if(unitUS.equals("F"))
            amountMetric = (amountUS - 32)/1.8;
        else if(unitUS.equals("lb"))
            amountMetric = amountUS / 2.2046;
        else if(unitUS.equals("oz"))
            amountMetric = amountUS / 0.035274;
        else if(unitUS.equals("floz") || unitUS.equals("fl oz"))
            amountMetric = amountUS / 0.033814;
        else if(unitUS.equals("gal"))
            amountMetric = amountUS / 0.26414;

        return String.format( "%.2f", amountMetric );
    }

    private String convertToUS(String amountStr, String unitMetric){

        double amountMetric = Double.parseDouble(amountStr);
        double amountUS =  amountMetric;

        if(unitMetric.equals("C"))
            amountUS = amountMetric * 1.8 + 32;
        else if(unitMetric.equals("kg"))
            amountUS = amountMetric * 2.2046;
        else if(unitMetric.equals("gm"))
            amountUS = amountMetric * 0.035274;
        else if(unitMetric.equals("ml"))
            amountUS = amountMetric * 0.033814;
        else if(unitMetric.equals("l"))
            amountUS = amountMetric * 0.26414;

        return String.format( "%.2f", amountUS );
    }

    private String getMetricUnit(String unit){
        for(int i = 0; i < mUSUnits.length; i++){
            if(mUSUnits[i].equals(unit)){
                return mMetricUnits[i];
            }
        }
        return unit;
    }

    private String getUSUnit(String unit){
        for(int i = 0; i < mMetricUnits.length; i++){
            if(mMetricUnits[i].equals(unit)){
                return mUSUnits[i];
            }
        }
        return unit;
    }



}
