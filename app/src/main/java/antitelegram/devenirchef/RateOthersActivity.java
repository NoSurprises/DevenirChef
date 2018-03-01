package antitelegram.devenirchef;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import antitelegram.devenirchef.data.FinishedRecipe;
import antitelegram.devenirchef.utils.Constants;
import antitelegram.devenirchef.utils.Utils;

public class RateOthersActivity extends DrawerBaseActivity {

    private FirebaseUser currentUser;
    private Query query;

    private ImageView recipeImageBox;
    private TextView noRecipes;
    private RatingBar starRatingBar;

    private List<FinishedRecipe> recipeList;
    private List<String> usersId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.rate_others);
        bindViews();
        bindButtons();

        noRecipes.setVisibility(View.INVISIBLE);

        currentUser = Utils.getFirebaseAuth().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userUid = currentUser.getUid();
        final DatabaseReference currentUserReference = Utils.getFirebaseDatabase()
                .getReference(Constants.DATABASE_USERS)
                .child(userUid);

        recipeList = new ArrayList<>();
        usersId = new ArrayList<>();
        query = Utils.getFirebaseDatabase().getReference().child(Constants.DATABASE_USERS);

        refreshRecipes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        recipeList.clear();
        usersId.clear();
    }

    private void bindViews() {
        recipeImageBox = findViewById(R.id.RecipeImageBox);
        noRecipes = findViewById(R.id.no_recipes);
        starRatingBar = findViewById(R.id.ratingBar);
    }

    private ValueEventListener getExpSetter(final String index) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                FinishedRecipe recipe = dataSnapshot.child(Constants.FINISHED_RECIPES)
                        .child(index).getValue(FinishedRecipe.class);

                recipe.setRated();
                dataSnapshot.getRef().child(Constants.FINISHED_RECIPES).child(recipe.getIndex()).setValue(recipe);

                long newExp = dataSnapshot.child(Constants.EXP).getValue(Long.class) +
                        (long) recipe.getAverageRating() * Constants.RATING_MULTIPLIER * recipe.getLevel();

                dataSnapshot.getRef().child(Constants.EXP).setValue(newExp);

                int level = dataSnapshot.child(Constants.LEVEL).getValue(Integer.class);
                if (level < Constants.MAX_LEVEL && newExp >= Constants.EXP_LEVELS[level]) {
                    dataSnapshot.getRef().child(Constants.LEVEL).setValue(level + 1);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void bindButtons() {

        starRatingBar.setRating(0);
        starRatingBar.setStepSize(0.5f);

        starRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int intRating = (int) rating;
                if (rating - intRating >= 0.8) {
                    intRating++;
                }
                ratingBar.setRating(intRating);

                if (recipeList.size() == 0 || !fromUser) {
                    return;
                }


                // Get recipe
                FinishedRecipe recipe = recipeList.get(0);
                DatabaseReference userRef = FirebaseDatabase.getInstance().
                        getReference(Constants.DATABASE_USERS).child(usersId.get(0));

                // If recipe is already rated, continue
                if (recipe.isRated()) {
                    nextImage();
                    return;
                }

                // If recipe is not rated, but already has enough ratings => rate, then continue
                if (!recipe.isRated() && recipe.getUsersRated().size() >= Constants.RATINGS_FOR_EXP) {
                    nextImage();
                    userRef.addListenerForSingleValueEvent(getExpSetter(recipe.getIndex()));
                    return;
                }

                // Add current rating to the picture
                recipe.setAverageRating(
                        (recipe.getAverageRating() * recipe.getUsersRated().size() + intRating) /
                                (recipe.getUsersRated().size() + 1));
                recipe.addUsersRated(currentUser.getUid());
                userRef.child(Constants.FINISHED_RECIPES).child(recipe.getIndex()).setValue(recipe);

                // Rate if needed
                if (recipe.getUsersRated().size() >= Constants.RATINGS_FOR_EXP) {
                    userRef.addListenerForSingleValueEvent(getExpSetter(recipe.getIndex()));
                }
                // Continue
                nextImage();
            }
        });
    }

    private void nextImage() {
        recipeList.remove(0);
        usersId.remove(0);

        if (recipeList.isEmpty()) {
            starRatingBar.setRating(0);
            refreshRecipes();
        } else {
            setImage(recipeList.get(0), recipeImageBox);
        }
    }

    private void refreshRecipes() {

        ValueEventListener refresher = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!recipeList.isEmpty()) {
                    return;
                }

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    String user = userSnapshot.getKey();

                    if (user.equals(currentUser.getUid()))
                        continue;

                    for (DataSnapshot recipeSnapshot : userSnapshot.child("finishedRecipes").getChildren()) {
                        FinishedRecipe recipe = recipeSnapshot.getValue(FinishedRecipe.class);
                        if (!recipe.getPhotoUrl().equals("none")
                                && recipe.getUsersRated().size() < Constants.RATINGS_FOR_EXP
                                && !recipe.getUsersRated().contains(currentUser.getUid())) {
                            recipeList.add(recipe);
                            usersId.add(user);
                        }
                    }
                }
                if (recipeList.size() != 0) {
                    long seed = System.nanoTime();
                    Collections.shuffle(recipeList, new Random(seed));
                    Collections.shuffle(usersId, new Random(seed));

                    noRecipes.setVisibility(View.INVISIBLE);
                    recipeImageBox.setVisibility(View.VISIBLE);
                    setImage(recipeList.get(0), recipeImageBox);
                } else {
                    noRecipes.setVisibility(View.VISIBLE);
                    recipeImageBox.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(refresher);
    }

    private void setImage(final FinishedRecipe recipe, final ImageView image) {

        image.setImageResource(R.drawable.ic_placeholder_transparent);

        String photoUrl = recipe.getPhotoUrl();
        if (photoUrl.equals(Constants.NO_FILE_ADDED))
            return;

        StorageReference imageRef = Utils.getFirebaseStorage().getReference(photoUrl);
        Task<Uri> imageTask = imageRef.getDownloadUrl();
        imageTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (!isFinishing()) {

                    try {
                        Glide.with(RateOthersActivity.this)
                                .load(uri)
                                .error(R.drawable.ic_search_black_24dp)
                                .placeholder(R.drawable.ic_placeholder_transparent)
                                .dontAnimate()
                                .into(image);
                    } catch (Exception ignored) {

                    }

                }

                starRatingBar.setRating(0);
            }
        });
    }

    @Override
    void addDatabaseReadListener() {

    }
}
