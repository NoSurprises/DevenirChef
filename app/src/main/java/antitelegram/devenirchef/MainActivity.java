package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import antitelegram.devenirchef.data.Recipe;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "daywint";
    private static final int RC_SIGN_IN = 123;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private LinearLayout recipesLayout;


    private List<Recipe> recipes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.options_menu);


        recipesLayout = findViewById(R.id.recipes_linear_layout);
        recipes = new ArrayList<>();

        initializeDatabase();
        initializeStorage();
        initializeAuth();


    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }

    private void initializeAuth() {
        // TODO: 1/7/2018 decompose
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(MainActivity.this, "signed in " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {

                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                    );

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);

                }

            }
        };
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                FirebaseUser user = auth.getCurrentUser();
                Toast.makeText(this, "signed in as " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeStorage() {
        storage = FirebaseStorage.getInstance();
    }

    private void initializeDatabase() {
        database = FirebaseDatabase.getInstance();

        database.getReference("recipes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                View recipeView = createNewRecipeCard();
                Recipe newRecipe = dataSnapshot.getValue(Recipe.class);
                recipes.add(newRecipe);
                bindDataToViewFromRecipe(recipeView, newRecipe);
            }

            private void bindDataToViewFromRecipe(View view, final Recipe newRecipe) {

                View.OnClickListener listener = new View.OnClickListener() {
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
                ImageView imageView = view.findViewById(R.id.recipe_image);


                // bind data
                text.setText(newRecipe.getTitle());
                // TODO: 12/30/2017 bind description, steps..
                setImageToView(imageView, newRecipe.getPhotoUrl());


                // bind listeners
                text.setOnClickListener(listener);
                imageView.setOnClickListener(listener);


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
        });
    }


    private View createNewRecipeCard() {
        View newRecipe = getLayoutInflater().inflate(R.layout.recipe_main_screen, recipesLayout, false);
        recipesLayout.addView(newRecipe);
        return newRecipe;
    }

    private void setImageToView(ImageView image, String photoUrl) {


        if (!isFinishing()) {
            Glide.with(image.getContext())
                    .load(photoUrl)
                    .into(image);
        }
    }


}
