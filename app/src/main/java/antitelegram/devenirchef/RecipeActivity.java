package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import antitelegram.devenirchef.cooking.CookActivity;
import antitelegram.devenirchef.data.Recipe;

/**
 * Created by Nick on 11/27/2017.
 */

public class RecipeActivity extends AppCompatActivity {

    private Recipe recipe;
    private Intent fromIntent;
    private int cookRequestCode = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_activity);

        fromIntent = getIntent();
        if (fromIntent == null)
            return;

        recipe = getRecipe();
        bindInfoToViews();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cook_action:
                launchCookActivity();
                return true;

        }
        return false;
    }

    private void launchCookActivity() {
        Intent intent = new Intent(RecipeActivity.this, CookActivity.class);
        intent.putExtra("recipe", recipe);
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

    private Recipe getRecipe() {
        return fromIntent.getExtras().getParcelable("recipe");
    }

    private void bindInfoToViews() {
        ((TextView) findViewById(R.id.recipe_name)).setText(recipe.getTitle());
        // TODO: 1/2/2018 bind other fields

        // bind image
        ImageView image = (ImageView) findViewById(R.id.recipe_image);

        if (!isFinishing()) {
            Glide.with(image.getContext())
                    .load(recipe.getPhotoUrl())
                    .into(image);
        }
    }


}
