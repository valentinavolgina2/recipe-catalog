//CSD 230 Final Project - Valentina Volgina

package edu.lwtech.finalp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class TypeActivity extends AppCompatActivity
        implements TypeDialogFragmant.OnTypeEnteredListener{

    private RecipeDatabase mRecipeDb;
    private TypeActivity.TypeAdapter mTypeAdapter;
    private RecyclerView mRecyclerView;
    private int[] mTypeColors;

    private Type mSelectedType;
    private int mSelectedTypePosition = RecyclerView.NO_POSITION;
    private ActionMode mActionMode = null;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        mTypeColors = getResources().getIntArray(R.array.subjectColors);

        // Singleton
        mRecipeDb = RecipeDatabase.getInstance(getApplicationContext());

        mRecyclerView = findViewById(R.id.subjectRecyclerView);

        // Create 2 grid layout columns
        RecyclerView.LayoutManager gridLayoutManager =
                new GridLayoutManager(getApplicationContext(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        // Shows the available subjects
        mTypeAdapter = new TypeActivity.TypeAdapter(loadTypes());
        mRecyclerView.setAdapter(mTypeAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load subjects here in case settings changed
        mTypeAdapter = new TypeActivity.TypeAdapter(loadTypes());
        mRecyclerView.setAdapter(mTypeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.type_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                intent = new Intent(TypeActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onTypeEntered(String type) {
        // Returns subject entered in the SubjectDialogFragment dialog
        if (type.length() > 0) {
            if(typeExists(type)){
                showWarning(type);
            }else{
                Type t = new Type(type);
                mRecipeDb.typeDao().insertType(t);
                mTypeAdapter.addType(t);
            }

        }
    }

    private boolean typeExists(String typeName){

        //check if a catalog with the same name already exists
        Type type = mRecipeDb.typeDao().getType(typeName);
        return (type!=null);
    }

    @Override
    public void onTypeEdited(String type) {
        // Returns subject entered in the SubjectDialogFragment dialog
        if (type.length() > 0) {

            if(typeExists(type)){
                showWarning(type);
            }else{
                Type t = new Type(type);
                mRecipeDb.typeDao().insertType(t);

                // change field "catalog" for all the recipe of the edited catalog
                List<Recipe> typeRecipes = mRecipeDb.recipeDao().getRecipes(mSelectedType.getText());
                for (Recipe recipe : typeRecipes) {
                    recipe.setType(type);
                    mRecipeDb.recipeDao().updateRecipe(recipe);
                }

                // second, delete the subject (and from RecyclerView)
                mRecipeDb.typeDao().deleteType(mSelectedType);
                mTypeAdapter.removeType(mSelectedType);
                mTypeAdapter.addType(t);

            }
        }
    }

    private void showWarning(String typeName){
        new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(getResources().getString(R.string.type_exists,typeName))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FragmentManager manager = getSupportFragmentManager();
                        TypeDialogFragmant previousDialog = new TypeDialogFragmant();
                        previousDialog.catalogName = (mSelectedType==null) ? "" : mSelectedType.getText();
                        previousDialog.show(manager, "typeDialog");
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(R.string.cancel, null)
                .setIcon(R.drawable.ic_warning_24dp)
                .show();
    }

    public void addTypeClick(View view) {
        // Prompt user to type new subject
        FragmentManager manager = getSupportFragmentManager();
        TypeDialogFragmant dialog = new TypeDialogFragmant();
        dialog.show(manager, "typeDialog");
    }

    private void editCatalog(){
        // Prompt user to edit current subject
        FragmentManager manager = getSupportFragmentManager();
        TypeDialogFragmant dialog = new TypeDialogFragmant();
        dialog.catalogName = mSelectedType.getText();
        dialog.show(manager, "typeDialog");
    }

    private List<Type> loadTypes() {
        String order = mSharedPrefs.getString(SettingsFragment.PREFERENCE_SUBJECT_ORDER, "1");
        switch (Integer.parseInt(order)) {
            case 0: return mRecipeDb.typeDao().getTypes();
            case 1: return mRecipeDb.typeDao().getTypesNewerFirst();
            default: return mRecipeDb.typeDao().getTypesOlderFirst();
        }
    }

    private class TypeHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener{

        private Type mType;
        private TextView mTextView;

        public TypeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.recycler_view_items, parent, false));
            itemView.setOnClickListener(this);
            mTextView = itemView.findViewById(R.id.subjectTextView);

            itemView.setOnLongClickListener(this);
        }

        public void bind(Type type, int position) {
            mType = type;
            mTextView.setText(type.getText());

            if (mSelectedTypePosition == position) {
                // Make selected subject stand out
                mTextView.setBackgroundColor(Color.rgb(0,0,0));
            } else {
                // Make the background color dependent on the length of the subject string
                int colorIndex = type.getText().length() % mTypeColors.length;
                mTextView.setBackgroundColor(mTypeColors[colorIndex]);
            }
        }

        @Override
        public void onClick(View view) {
            // Start QuestionActivity, indicating what subject was clicked
            Intent intent = new Intent(TypeActivity.this, RecipeActivity.class);
            intent.putExtra(RecipeActivity.EXTRA_TYPE, mType.getText());
            startActivity(intent);
        }

        @Override
        public boolean onLongClick(View view) {
            if (mActionMode != null) {
                return false;
            }

            mSelectedType = mType;
            mSelectedTypePosition = getAdapterPosition();

            // Re-bind the selected item
            mTypeAdapter.notifyItemChanged(mSelectedTypePosition);

            // Show the CAB
            mActionMode = TypeActivity.this.startActionMode(mActionModeCallback);

            return true;
        }
    }


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Provide context menu for CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Process action item selection
            switch (item.getItemId()) {
                case R.id.delete:
                    // first, delete all the recipes of the category
                    List<Recipe> typeRecipes = mRecipeDb.recipeDao().getRecipes(mSelectedType.getText());
                    for (Recipe recipe : typeRecipes) {

                        //delete all the ingredients first
                        final List<Ingredient> deletedIngredients = mRecipeDb.ingredientDao().getIngredients(recipe.getId());
                        for(Ingredient ingredient : deletedIngredients){
                            mRecipeDb.ingredientDao().deleteIngredient(ingredient);
                        }

                        //delete the recipe
                        mRecipeDb.recipeDao().deleteRecipe(recipe);
                    }

                    // second, delete the category (and from RecyclerView)
                    mRecipeDb.typeDao().deleteType(mSelectedType);
                    mTypeAdapter.removeType(mSelectedType);

                    // Close the CAB
                    mode.finish();
                    return true;
                case R.id.edit:

                    editCatalog();

                    // Close the CAB
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;

            // CAB closing, need to deselect item if not deleted
            mTypeAdapter.notifyItemChanged(mSelectedTypePosition);
            mSelectedTypePosition = RecyclerView.NO_POSITION;
        }
    };

    private class TypeAdapter extends RecyclerView.Adapter<TypeActivity.TypeHolder> {

        private List<Type> mTypeList;

        public TypeAdapter(List<Type> subjects) {
            mTypeList = subjects;
        }

        @Override
        public TypeActivity.TypeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new TypeActivity.TypeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(TypeActivity.TypeHolder holder, int position){
            holder.bind(mTypeList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mTypeList.size();
        }

        public void addType(Type type) {
            // Add the new subject at the beginning of the list
            mTypeList.add(0, type);

            // Notify the adapter that item was added to the beginning of the list
            notifyItemInserted(0);

            // Scroll to the top
            mRecyclerView.scrollToPosition(0);
        }

        public void removeType(Type subject) {
            // Find subject in the list
            int index = mTypeList.indexOf(subject);
            if (index >= 0) {
                // Remove the subject
                mTypeList.remove(index);

                // Notify adapter of subject removal
                notifyItemRemoved(index);
            }
        }

    }



}
