package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Nick on 11/27/2017.
 */

public class RecipeActivity extends AppCompatActivity {

    public final static String NAME_KEY = "name";
    public final static String DESCRIPTION_KEY = "description";
    public final static String ID_KEY = "id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent fromIntent = getIntent();
        if (fromIntent == null) {
            return;
        }
        String name = fromIntent.getStringExtra(NAME_KEY);
        String description = fromIntent.getStringExtra(DESCRIPTION_KEY);
        int id = fromIntent.getIntExtra(ID_KEY, -1);

        // todo get recipe from db based on the id provided

    }
}
