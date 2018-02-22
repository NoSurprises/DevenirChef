package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import antitelegram.devenirchef.cooking.CookActivity;
import antitelegram.devenirchef.data.Recipe;

/**
 * Created by Nick on 11/27/2017.
 */

public class RecipeActivity extends AppCompatActivity {

    private static final String RECIPE_KEY = "recipe";
    private Recipe recipe;
    private Intent fromIntent;
    private int cookRequestCode = 1;
    private TextView recipeTitle;
    private TextView description;
    private TextView ingredients;
    private LinearLayout starsContainer;
    private FloatingActionButton cook;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_activity);

        fromIntent = getIntent();
        if (fromIntent == null)
            return;

        recipe = getRecipeFromIntent();

        bindInfoToViews();
        addListeners();
    }

    private void addListeners() {
        cook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCookActivity();
            }
        });
    }

    private void launchCookActivity() {
        Intent intent = new Intent(RecipeActivity.this, CookActivity.class);
        intent.putExtra(RECIPE_KEY, recipe);
        startActivityForResult(intent, cookRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == cookRequestCode && resultCode == RESULT_OK) {
            // cooking was successful. get to the place, where this recipe came from
            this.finish();
        }
    }

    private Recipe getRecipeFromIntent() {
        return fromIntent.getExtras().getParcelable(RECIPE_KEY);
    }

    private void bindInfoToViews() {

        recipeTitle = findViewById(R.id.recipe_name);
        description = findViewById(R.id.recipe_description);
        ingredients = findViewById(R.id.recipe_ingredients);
        starsContainer = findViewById(R.id.recipe_star_container);
        cook = findViewById(R.id.cook_action_button);


        // bind data
        recipeTitle.setText(recipe.getTitle());
        description.setText(recipe.getDescription());
        ingredients.setText(recipe.getIngredients());
        LayoutInflater inflater = getLayoutInflater();
        for (int i = 1; i < recipe.getLevel(); i++) {

            inflater.inflate(R.layout.recipe_star, starsContainer, true);
        }
        // bind image
        ImageView image = findViewById(R.id.recipe_image);

        if (!isFinishing()) {
            Glide.with(this)
                    .load(recipe.getPhotoUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .crossFade()
                    .into(image);
        }
    }


}
