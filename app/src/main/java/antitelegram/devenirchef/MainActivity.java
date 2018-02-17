package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import antitelegram.devenirchef.data.Recipe;
import antitelegram.devenirchef.utils.Utils;

public class MainActivity extends DrawerBaseActivity {

    public static final String TAG = "daywint";
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth.AuthStateListener authStateListener;
    private LinearLayout recipesLayout;


    private List<Recipe> recipes;
    private ChildEventListener childEventListener;
    private ScrollView recipesScroll;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.main_content);

        initRecipesStorage();

        setToolbarClickedListener();
    }

    private void initRecipesStorage() {
        recipesLayout = findViewById(R.id.recipes_linear_layout);
        recipesScroll = findViewById(R.id.scroll_recipes_view);
        recipes = new ArrayList<>();

    }

    private void removeAllRecipes() {
        recipes.clear();
        recipesLayout.removeAllViews();
    }

    private void setToolbarClickedListener() {
        View.OnClickListener toolbarClicked = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipesScroll.fullScroll(ScrollView.FOCUS_UP);
            }
        };
        setOnToolbarClickedListener(toolbarClicked);
    }



    @Override
    protected void onPause() {
        super.onPause();
        removeAllRecipes();
    }

    void addDatabaseReadListener() {
        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {


                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    View recipeView = createNewRecipeCard();
                    Recipe newRecipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(newRecipe);
                    bindDataToViewFromRecipe(recipeView, newRecipe);
                }

                private void bindDataToViewFromRecipe(View view, final Recipe newRecipe) {

                    View.OnClickListener openRecipeActivity = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
                            intent.putExtra("recipe", newRecipe);
                            startActivity(intent);
                        }
                    };


                    // TODO: 1/2/2018 decompose

                    // get data
                    TextView text = view.findViewById(R.id.recipe_name);
                    LinearLayout starsContainer = view.findViewById(R.id.recipe_star_container);
                    ImageView recipeImage = view.findViewById(R.id.recipe_image);
                    TextView description = view.findViewById(R.id.recipe_description);
                    LinearLayout tags = view.findViewById(R.id.tags_container);


                    // bind data
                    text.setText(newRecipe.getTitle());
                    int level = newRecipe.getLevel();
                    LayoutInflater layoutInflater = getLayoutInflater();

                    // start from 1, because 1 star is already in xml
                    for (int i = 1; i < level; i++) {
                        layoutInflater.inflate(R.layout.recipe_star, starsContainer, true);
                    }
                    setImageToView(recipeImage, newRecipe.getPhotoUrl());
                    description.setText(newRecipe.getDescription());

                    if (newRecipe.getTags() != null) {
                        for (String tag : newRecipe.getTags()) {
                            TextView tagView = new TextView(view.getContext());
                            tagView.setText(tag);
                            tags.addView(tagView);
                        }

                    }
                    // bind listeners
                    text.setOnClickListener(openRecipeActivity);
                    recipeImage.setOnClickListener(openRecipeActivity);


                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
        }
        Utils.getFirebaseDatabase().getReference("recipes").addChildEventListener(childEventListener);
    }


    @Override
    void onSignedOut() {
        super.onSignedOut();
        removeAllRecipes();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                FirebaseUser user = Utils.getFirebaseAuth().getCurrentUser();
            } else {
                Toast.makeText(this, "failed " + resultCode, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private View createNewRecipeCard() {
        View newRecipe = getLayoutInflater().inflate(R.layout.recipe_main_screen, recipesLayout, false);
        recipesLayout.addView(newRecipe);
        return newRecipe;
    }

    private void setImageToView(ImageView image, String photoUrl) {
        if (!isFinishing()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .crossFade()
                    .into(image);
        }
    }
}
