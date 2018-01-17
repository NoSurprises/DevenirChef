package antitelegram.devenirchef.cooking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import antitelegram.devenirchef.R;
import antitelegram.devenirchef.data.FinishedRecipe;
import antitelegram.devenirchef.data.Recipe;
import antitelegram.devenirchef.data.RecipeStep;
import antitelegram.devenirchef.data.User;

public class CookActivity extends FragmentActivity {


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook);

        initDatabase();
        initAuth();

        setUpViewPager();
        setUpRecipe();

        // to update count info
        setNewDatasetSize();

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("recipe", recipe);

    }

    public void saveImageToDatabase(Bitmap image) {
        usersValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: value " + dataSnapshot.getValue());
                if (!dataSnapshot.exists()) {
                    userReference.setValue(new User());
                    Log.d(TAG, "onDataChange: created new user in database");
                }
                addNewFinishedRecipe();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userReference.addListenerForSingleValueEvent(usersValueListener);
    }

    private void addNewFinishedRecipe() {
        FinishedRecipe finishedRecipe = new FinishedRecipe();
        finishedRecipe.setTitle(recipe.getTitle());
        finishedRecipe.setPhotoUrl("stuub!");

        userReference.child("finishedRecipes").push().setValue(finishedRecipe);
    }


    private void initAuth() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        String userUid = user.getUid();
        userReference = firebaseDatabase.getReference("users/" + userUid);

    }

    private void initDatabase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
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

    private Recipe getRecipe() {
        return fromIntent.getExtras().getParcelable("recipe");
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
            cookingStep.setRecipeStep(recipeStepData);

            return cookingStep;
        }


        @Override
        public int getCount() {
            return numSteps;
        }


    }

}
