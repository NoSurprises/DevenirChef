package antitelegram.devenirchef;

import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "daywint";
    private FirebaseDatabase database;
    private LinearLayout recipesLayout;
    private int tmpCount = 0; // TODO: 12/30/2017 keeps track of the current
    // added child. remove, when have stored images in cloud

    private List<Recipe> recipes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recipesLayout = (LinearLayout) findViewById(R.id.recipes_linear_layout);
        recipes = new ArrayList<>();
        database = FirebaseDatabase.getInstance();

        database.getReference("recipes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                View recipeView = createRecipeView();
                Log.d(TAG, "onChildAdded: ");

                Recipe newRecipe = dataSnapshot.getValue(Recipe.class);
                bindDataToViewFromRecipe(recipeView, newRecipe);

                recipes.add(newRecipe);
            }

            private void bindDataToViewFromRecipe(View view, Recipe newRecipe) {
                ((TextView) view.findViewById(R.id.recipe_name)).setText(newRecipe.getTitle());
                // TODO: 12/30/2017 bind description, steps..
                // TODO: 12/30/2017 bind image!
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

    private View createRecipeView() {
        View newRecipe = createNewRecipeCard();
        setStoredImageToView(tmpCount++, newRecipe);
        return newRecipe;
    }


    private View createNewRecipeCard() {
        View newRecipe = getLayoutInflater().inflate(R.layout.recipe_main_screen, recipesLayout, false);
        recipesLayout.addView(newRecipe);
        return newRecipe;
    }

    private void setStoredImageToView(int i, View newRecipe) {
        int[] tempDrawables = {R.drawable.burger, R.drawable.fish, R.drawable.pancakes, R.drawable.pumpkins, R.drawable.soup};

        ((ImageView) newRecipe.findViewById(R.id.recipe_image)).setImageDrawable(ResourcesCompat.getDrawable(
                getResources(), tempDrawables[i], null)
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
}
