package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by Nick on 11/27/2017.
 */

public class RecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_activity);
        Intent fromIntent = getIntent();
        if (fromIntent == null) {
            return;
        }
        Recipe recipe = fromIntent.getExtras().getParcelable("recipe");

        ((TextView) findViewById(R.id.recipe_name)).setText(recipe.getTitle());
        // TODO: 1/2/2018 bind other fields

        ImageView image = (ImageView) findViewById(R.id.recipe_image);
        Glide.with(this)
                .load(recipe.getPhotoUrl())
                .into(image);


    }
}
