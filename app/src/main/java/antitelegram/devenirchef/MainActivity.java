package antitelegram.devenirchef;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import antitelegram.devenirchef.data.Recipe;
import antitelegram.devenirchef.utils.Constants;
import antitelegram.devenirchef.utils.Utils;

public class MainActivity extends DrawerBaseActivity {

    public static final String TAG = "daywint";
    private static final int RC_SIGN_IN = 123;

    private RecyclerView recipesLayout;


    private List<Recipe> recipes;
    private List<String> selectedTags = new ArrayList<>();
    private int selectedComplexity = 1;
    private ChildEventListener childEventListener;
    private RelativeLayout bottomMenuExpanded;
    private RelativeLayout bottomMenuShrinked;

    private boolean bottomExpanded = false;
    private RecipesAdapter recipesAdapter;
    private ViewGroup tagsContainer;

    private SearchView searchView;

    public static void expand(final View v, final int fromHeight, final View hideBefore) {
        v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.

        hideBefore.setVisibility(View.GONE);
        v.getLayoutParams().height = fromHeight;
        v.requestLayout();
        v.setVisibility(View.VISIBLE);


        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    v.getLayoutParams().height = fromHeight + (int) ((targetHeight - fromHeight) * interpolatedTime);
                }
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd: expand end");
                v.setVisibility(View.VISIBLE);
                hideBefore.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);
    }

    public static void collapse(final View expanded, final int toHeight, final View showAfter) {
        expanded.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final int initialHeight = expanded.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                expanded.getLayoutParams().height = initialHeight - (int) ((initialHeight - toHeight) * interpolatedTime);

                expanded.requestLayout();

            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / expanded.getContext().getResources().getDisplayMetrics().density));
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd: collapse end");
                expanded.setVisibility(View.GONE);
                showAfter.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        expanded.startAnimation(a);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.main_content);

        initRecipesStorage();
        setUpBottomMenu();
        setToolbarClickedListener();

    }

    private void setUpBottomMenu() {
        bottomMenuExpanded = findViewById(R.id.bottom_menu_expanded);
        bottomMenuShrinked = findViewById(R.id.bottom_menu_shrinked);
        View.OnClickListener toggle = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomExpanded) {
                    collapse(bottomMenuExpanded, bottomMenuShrinked.getHeight(), bottomMenuShrinked);
                    setShrinkedComplexity();
                    setShrinkedTags();
                    selectRecipesComplexityAndTags();

                } else {
                    expand(bottomMenuExpanded, bottomMenuShrinked.getHeight(), bottomMenuShrinked);
                }
                bottomExpanded = !bottomExpanded;
            }
        };
        bottomMenuShrinked.findViewById(R.id.arrow_button).setOnClickListener(toggle);
        bottomMenuExpanded.findViewById(R.id.arrow_button).setOnClickListener(toggle);
        tagsContainer = bottomMenuExpanded.findViewById(R.id.tags_container);

        setUpTags(Arrays.asList(Constants.TAGS));
        setUpComplexitySeekbar();

    }

    private void selectRecipesComplexityAndTags() {
        if (selectedTags.size() == 0) {
            recipesAdapter.changeDataset(recipes);
            return;
        }
        List<Recipe> newDataset = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getLevel() <= selectedComplexity &&
                    !Collections.disjoint(selectedTags, recipe.getTags())) {
                newDataset.add(recipe);
            }
        }
        recipesAdapter.changeDataset(newDataset);
    }

    private void setShrinkedComplexity() {
        ((TextView) bottomMenuShrinked.findViewById(R.id.complexity_button_all)).setText(selectedComplexity + "");
    }

    private void setShrinkedTags() {
        if (selectedTags.size() > 0)
            ((TextView) bottomMenuShrinked.findViewById(R.id.course_button_all)).setText(selectedTags.get(0) + "...");
        else
            ((TextView) bottomMenuShrinked.findViewById(R.id.course_button_all)).setText(Constants.ALL);

    }

    private void setUpComplexitySeekbar() {
        final SeekBar complexity = bottomMenuExpanded.findViewById(R.id.complexity_picker);
        complexity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                selectedComplexity = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private void setUpTags(List<String> tags) {
        for (String tag : tags) {
            View tagView = getLayoutInflater().inflate(R.layout.tag, tagsContainer, false);
            ((TextView) tagView.findViewById(R.id.tag_name)).setText(tag);
            tagsContainer.addView(tagView);
            tagView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String tagName = ((TextView) view.findViewById(R.id.tag_name)).getText().toString();
                    if (selectedTags.contains(tagName)) {
                        view.setBackgroundColor(getResources().getColor(R.color.transparent));
                        selectedTags.remove(tagName);
                    } else {
                        view.setBackgroundColor(getResources().getColor(R.color.selected_tag));
                        selectedTags.add(tagName);
                    }
                }
            });

        }
    }

    private void initRecipesStorage() {
        recipes = new ArrayList<>();

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        recipesLayout = findViewById(R.id.recipes_layout);
        recipesLayout.setLayoutManager(new LinearLayoutManager(this));
        recipesAdapter = new RecipesAdapter(this);
        recipesLayout.setAdapter(recipesAdapter);
        recipesAdapter.changeDataset(recipes);
    }

    private void removeAllRecipes() {
        recipes.clear();
        recipesLayout.removeAllViews();
    }

    private void setToolbarClickedListener() {
        View.OnClickListener toolbarClicked = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipesLayout.smoothScrollToPosition(0);
            }
        };
        setOnToolbarClickedListener(toolbarClicked);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        MenuItem item = menu.findItem(R.id.search);
        searchView = (SearchView) item.getActionView();
        Log.d("search", "hello");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                recipesAdapter.changeDataset(searchFilter(query));

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recipesAdapter.changeDataset(searchFilter(newText));

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private List<Recipe> searchFilter(final String query) {
        List<Recipe> res = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getTitle().toLowerCase().contains(query)) {
                res.add(recipe);
            }
            else {
                for (String tag : recipe.getTags()) {
                    if (tag.toLowerCase().contains(query)) {
                        res.add(recipe);
                        break;
                    }
                }
            }
        }

        return res;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (searchView != null) {
            //searchView.setQuery("WHAT", true);
            Log.d("search", searchView.getQuery().toString());
            recipesAdapter.changeDataset(searchFilter("WHAT"));

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeAllRecipes();
    }

    void addDatabaseReadListener() {
        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    Recipe newRecipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(newRecipe);
                    recipesAdapter.changeDataset(recipes);
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
        Utils.getFirebaseDatabase().getReference("recipes").addChildEventListener(childEventListener);
    }


    @Override
    void onSignedOut() {
        super.onSignedOut();
        removeAllRecipes();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == ResultCodes.OK) {
                FirebaseUser user = Utils.getFirebaseAuth().getCurrentUser();
            } else {
                Toast.makeText(this, "failed " + resultCode, Toast.LENGTH_SHORT).show();
            }
        }
    }


}
