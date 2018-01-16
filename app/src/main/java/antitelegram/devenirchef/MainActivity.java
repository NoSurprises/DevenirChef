package antitelegram.devenirchef;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import static com.google.android.gms.common.ConnectionResult.SUCCESS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "daywint";
    private static final int RC_SIGN_IN = 123;

    private NavigationView navigation;
    private DrawerLayout drawer;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private LinearLayout recipesLayout;


    private List<Recipe> recipes;
    private Toolbar toolbar;
    private ChildEventListener childEventListener;
    private View navigationHeader;
    private TextView drawerUsername;
    private TextView drawerEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpNavigationDrawer();
        setUpToolbar();
        initRecipesStorage();
        initDatabase();
        initStorage();
        initAuth();


    }

    private void setUpNavigationDrawer() {
        drawer = findViewById(R.id.drawer_layout);
        navigation = findViewById(R.id.nav_view);
        navigationHeader = navigation.getHeaderView(0);
        initDrawerDataFields();
        setNavigationMenuClickListener();
        setUpEmailOptionsButton();
    }

    private void setUpEmailOptionsButton() {
        ImageButton im = navigationHeader.findViewById(R.id.email_options_button);

        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu emailOptions = createPopupMenu(v);
                emailOptions.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.logout: {
                                logout();
                                drawer.closeDrawers();
                                return true;
                            }

                        }
                        return false;
                    }
                });

            }

            private PopupMenu createPopupMenu(View v) {
                PopupMenu emailOptions = new PopupMenu(MainActivity.this, v, Gravity.END);
                emailOptions.inflate(R.menu.email_options_menu);
                emailOptions.show();
                return emailOptions;
            }
        });
    }

    private void logout() {
        if (currentUser != null) {
            AuthUI.getInstance().signOut(this);
        }

    }

    private void setNavigationMenuClickListener() {
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                drawer.closeDrawers();
                Toast.makeText(MainActivity.this, item.getTitle() + " not implemented by Alex yet", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void initRecipesStorage() {
        recipesLayout = findViewById(R.id.recipes_linear_layout);
        recipes = new ArrayList<>();
    }

    private void setUpToolbar() {
        initToolbar();
        setUpDrawerToggle();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(MainActivity.this, "toolbar menu item clicked", Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }

    private void setUpDrawerToggle() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void removeAllRecipes() {
        recipes.clear();
        recipesLayout.removeAllViews();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.options_menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
        removeDatabaseReadListener();
        removeAllRecipes();
    }

    private void initAuth() {
        // TODO: 1/7/2018 decompose
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInInitialize(user);
                } else {

                    onSignedOut();
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                    );


                    GoogleApiAvailability apiAvailability = GoogleApiAvailability
                            .getInstance();
                    int codeServicesAvailable =
                            apiAvailability
                                    .isGooglePlayServicesAvailable(MainActivity.this);

                    if (codeServicesAvailable == SUCCESS) {
                        startActivityForResult(

                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setIsSmartLockEnabled(false)
                                        .setAvailableProviders(providers)
                                        .build(),
                                RC_SIGN_IN);
                    } else {
                        apiAvailability.showErrorDialogFragment(MainActivity.this, codeServicesAvailable, RC_SIGN_IN);
                    }

                }

            }
        };
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        Integer resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, resultCode, 0);
            if (dialog != null) {
                dialog.show();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                FirebaseUser user = auth.getCurrentUser();
            } else {
                Toast.makeText(this, "failed " + resultCode, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initStorage() {
        storage = FirebaseStorage.getInstance();
    }

    private void initDatabase() {
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
    }

    private void addDatabaseReadListener() {
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
                    LinearLayout starsContainer = view.findViewById(R.id.recipe_star_container);
                    ImageView recipeImage = view.findViewById(R.id.recipe_image);
                    TextView description = view.findViewById(R.id.recipe_description);


                    // bind data
                    text.setText(newRecipe.getTitle());

                    int level = newRecipe.getLevel();
                    LayoutInflater layoutInflater = getLayoutInflater();
                    for (int i = 1; i < level; i++) {
                        layoutInflater.inflate(R.layout.recipe_star, starsContainer, true);
                    }
                    setImageToView(recipeImage, newRecipe.getPhotoUrl());
                    description.setText(newRecipe.getDescription());

                    // bind listeners
                    text.setOnClickListener(listener);
                    recipeImage.setOnClickListener(listener);


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
        database.getReference("recipes").addChildEventListener(childEventListener);
    }

    private void removeDatabaseReadListener() {
        if (childEventListener != null) {
            database.getReference("recipes").removeEventListener(childEventListener);
        }
        childEventListener = null;
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

    private void onSignedInInitialize(FirebaseUser user) {
        currentUser = user;
        addDatabaseReadListener();
        initDataInNavigationDrawer();
    }

    private void initDataInNavigationDrawer() {
        drawerUsername.setText(currentUser.getDisplayName());
        drawerEmail.setText(currentUser.getEmail());
    }

    private void initDrawerDataFields() {
        drawerUsername = navigationHeader.findViewById(R.id.username);
        drawerEmail = navigationHeader.findViewById(R.id.user_email);
    }


    private void onSignedOut() {
        currentUser = null;
        removeAllRecipes();
        removeDatabaseReadListener();
    }


}
