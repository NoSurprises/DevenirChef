package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class CookActivity extends FragmentActivity {


    private static final int NUM_STEPS = 5;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;


    private Intent fromIntent;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook);

        setUpViewPager();
        setUpRecipe();

    }

    private void setUpRecipe() {
        fromIntent = getIntent();
        recipe = getRecipe();


    }

    private void setUpViewPager() {
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new CookingPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
    }


    private Recipe getRecipe() {
        return fromIntent.getExtras().getParcelable("recipe");
    }


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0)
            super.onBackPressed();
        else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private class CookingPagerAdapter extends FragmentStatePagerAdapter {

        public CookingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new RecipeCookingStepFragment();
        }

        @Override
        public int getCount() {
            return NUM_STEPS;
        }


    }

}
