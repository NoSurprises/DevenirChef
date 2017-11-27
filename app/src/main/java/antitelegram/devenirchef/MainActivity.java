package antitelegram.devenirchef;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        int[] tempDrawables = {R.drawable.burger,R.drawable.fish,R.drawable.pancakes,R.drawable.pumpkins,R.drawable.soup };
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.recipes_linear_layout);
        for (int i = 0; i < 5; i++) {
            View view = getLayoutInflater().inflate(R.layout.recipe_main_screen, linearLayout, false);
            linearLayout.addView(view);


            ((ImageView) view.findViewById(R.id.recipe_image)).setImageDrawable(ResourcesCompat.getDrawable(
                    getResources(), tempDrawables[i], null)
            );


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }
}
