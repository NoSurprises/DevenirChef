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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import antitelegram.devenirchef.R;
import antitelegram.devenirchef.data.FinishedRecipe;
import antitelegram.devenirchef.data.Recipe;
import antitelegram.devenirchef.data.RecipeStep;
import antitelegram.devenirchef.data.User;
import antitelegram.devenirchef.utils.Constants;
import antitelegram.devenirchef.utils.Utils;

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

    public void saveImageToDatabase(final Bitmap image) {

        usersValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Log.d(TAG, "onDataChange: value in snapshot " + dataSnapshot.getValue());

                    User user = getUser(dataSnapshot);
                    initUserFinishedRecipesIfNull(user);

                    updateUserInDatabase(user);
                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: can't add finished recipe to database " + e);
                    e.printStackTrace();
                }
            }

            private User getUser(DataSnapshot dataSnapshot) {
                User user;
                if (!dataSnapshot.exists()) {
                    user = new User();
                    Log.d(TAG, "onDataChange: created new user");
                } else {
                    user = dataSnapshot.getValue(User.class);
                }
                return user;
            }


            private void initUserFinishedRecipesIfNull(User user) {
                if (user.getFinishedRecipes() == null) {
                    user.setFinishedRecipes(new ArrayList<FinishedRecipe>());
                }
            }

            private void updateUserInDatabase(User user) {
                FinishedRecipe finishedRecipe = getFinishedRecipe();
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
        FirebaseStorage storage = FirebaseStorage.getInstance();
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
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        String userUid = user.getUid();
        userReference = firebaseDatabase.getReference("users/" + userUid);

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
