package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "daywint";
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private LinearLayout recipesLayout;


    private List<Recipe> recipes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recipesLayout = (LinearLayout) findViewById(R.id.recipes_linear_layout);
        recipes = new ArrayList<>();

        initializeDatabase();
        initializeStorage();


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
        Glide.with(this)
                .load(photoUrl)
                .into(image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
}
