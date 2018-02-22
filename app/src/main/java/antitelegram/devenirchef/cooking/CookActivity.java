package antitelegram.devenirchef.cooking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;

import antitelegram.devenirchef.R;
import antitelegram.devenirchef.data.CookingStepPreference;
import antitelegram.devenirchef.data.FinishedRecipe;
import antitelegram.devenirchef.data.Recipe;
import antitelegram.devenirchef.data.RecipeStep;
import antitelegram.devenirchef.data.SharedPreferencesModel;
import antitelegram.devenirchef.data.User;
import antitelegram.devenirchef.utils.Constants;
import antitelegram.devenirchef.utils.Utils;

public class CookActivity extends AppCompatActivity implements StepsNavigation {


    public static final String TAG = "daywint";
    private int numSteps;
    private ViewPager allStepsViewPager;
    private PagerAdapter pagerAdapter;
    private Intent fromIntent;
    private Recipe recipe;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener usersValueListener;
    private DatabaseReference userReference;
    private SharedPreferencesModel cookingState;
    private boolean needToSaveState = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook);

        initDatabase();
        initAuth();
        cookingState = new CookingStepPreference(getSharedPreferences("cooking", MODE_PRIVATE));
        Log.d(TAG, "onCreate: all in SP: ");
        for (Map.Entry<String, ?> entry : cookingState.all().entrySet()) {
            Log.d(TAG, entry.getKey() + " --- " + entry.getValue());
        }
        setUpRecipe();
        setUpViewPager();
        setUpToolbar();

        // to update count info
        setNewDatasetSize();

        restoreCookingStep();

    }

    @Override
    public void onBackPressed() {
        if (allStepsViewPager.getCurrentItem() == 0)
            super.onBackPressed();
        else {
            allStepsViewPager.setCurrentItem(allStepsViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onPause() {
        if (needToSaveState) {
            cookingState.saveStep(recipe.getTitle(), allStepsViewPager.getCurrentItem());
            Log.d(TAG, "saved state " + recipe.getTitle() + " " + allStepsViewPager.getCurrentItem());
        }
        super.onPause();
    }


    public void saveImageToDatabase(final Bitmap image) {

        usersValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.d(TAG, "onDataChange: value in snapshot " + dataSnapshot.getValue());

                    User user = getUser(dataSnapshot);
                    initUserFinishedRecipesIfNull(user);

                    addRecipeToUserInDatabase(user);
                    Log.d(TAG, "onDataChange: updated user info");
                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: can't add finished recipe to database " + e);
                    e.printStackTrace();
                }
            }

            private User getUser(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: created new user");
                    return new User();
                }
                return dataSnapshot.getValue(User.class);

            }


            private void initUserFinishedRecipesIfNull(User user) {
                if (user.getFinishedRecipes() == null) {
                    user.setFinishedRecipes(new ArrayList<FinishedRecipe>());
                }
            }

            private void addRecipeToUserInDatabase(User user) {
                FinishedRecipe finishedRecipe = getFinishedRecipe();
                finishedRecipe.setLevel(recipe.getLevel());
                user.getFinishedRecipes().add(finishedRecipe);
                userReference.setValue(user);
            }

            @NonNull
            private FinishedRecipe getFinishedRecipe() {
                FinishedRecipe finishedRecipe = new FinishedRecipe();
                setDataToRecipe(finishedRecipe);

                return finishedRecipe;
            }

            private void setDataToRecipe(FinishedRecipe finishedRecipe) {
                finishedRecipe.setTitle(recipe.getTitle());

                String photoUrl = uploadImageToStorage(image);
                finishedRecipe.setPhotoUrl(photoUrl);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        userReference.addListenerForSingleValueEvent(usersValueListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("recipe", recipe);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(recipe.getTitle());

        setUpMenu(toolbar);
        setUpTabs();
    }

    private void setUpMenu(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.cooking_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.abort:
                        setResult(RESULT_CANCELED);
                        cookingState.removeSavedState(recipe.getTitle());
                        needToSaveState = false;
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private String uploadImageToStorage(Bitmap image) {

        if (image == null) {
            return Constants.NO_FILE_ADDED;
        }
        byte[] byteImage = convertImageToByteArray(image);

        StorageReference imageRef = getReferenceToImage(image);
        imageRef.putBytes(byteImage);

        return imageRef.getPath();
    }

    @NonNull
    private StorageReference getReferenceToImage(Bitmap image) {
        StorageReference finished = getReferenceToFinishedRecipes();
        return finished.child(firebaseAuth.getCurrentUser().getUid() +
                image.hashCode());
    }

    @NonNull
    private StorageReference getReferenceToFinishedRecipes() {
        FirebaseStorage storage = Utils.getFirebaseStorage();
        return storage.getReference("finishedRecipes");
    }

    private byte[] convertImageToByteArray(Bitmap image) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);
        return byteStream.toByteArray();
    }

    // todo remove debug function
    public void removeUsersData() {
        userReference.removeValue();
    }


    private void initAuth() {
        firebaseAuth = Utils.getFirebaseAuth();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        String userUid = user.getUid();
        userReference = firebaseDatabase.getReference(Constants.DATABASE_USERS + "/" + userUid);

    }

    private void initDatabase() {
        firebaseDatabase = Utils.getFirebaseDatabase();
    }

    private void setNewDatasetSize() {
        numSteps = recipe.getCookingSteps().size();
        // adding last "finish" screen
        numSteps += 1;
        pagerAdapter.notifyDataSetChanged();
    }

    private void setUpRecipe() {
        fromIntent = getIntent();
        recipe = getRecipe();
    }

    private void setUpViewPager() {
        allStepsViewPager = findViewById(R.id.view_pager);
        pagerAdapter = new CookingPagerAdapter(getSupportFragmentManager());
        allStepsViewPager.setAdapter(pagerAdapter);
    }

    private void restoreCookingStep() {
        Log.d(TAG, "restore state " + cookingState.getSavedStep(recipe.getTitle()));
        allStepsViewPager.setCurrentItem(cookingState.getSavedStep(recipe.getTitle()));
    }

    private void setUpTabs() {
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(allStepsViewPager);
    }

    private Recipe getRecipe() {
        return fromIntent.getExtras().getParcelable("recipe");
    }

    @Override
    public void forward() {
        allStepsViewPager.setCurrentItem(allStepsViewPager.getCurrentItem() + 1);
    }

    @Override
    public void back() {
        allStepsViewPager.setCurrentItem(allStepsViewPager.getCurrentItem() - 1);
    }

    public void removeSavedState() {
        cookingState.removeSavedState(recipe.getTitle());
    }

    public void disableSavingState() {
        needToSaveState = false;
    }

    private class CookingPagerAdapter extends FragmentStatePagerAdapter {

        private static final String TAG = "daywint";

        public CookingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == numSteps - 1) {
                return new FinishRecipeStepFragment();
            }

            return createCookingStepFragment(position);
        }

        @NonNull
        private Fragment createCookingStepFragment(int position) {
            RecipeCookingStepFragment cookingStep = new RecipeCookingStepFragment();
            RecipeStep recipeStepData = recipe.getCookingSteps().get(position);
            recipeStepData.setStepNumber(position);
            cookingStep.setRecipeStep(recipeStepData);

            return cookingStep;
        }


        @Override
        public int getCount() {
            return numSteps;
        }

    }
}
