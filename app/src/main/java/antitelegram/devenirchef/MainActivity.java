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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import antitelegram.devenirchef.data.Recipe;
import antitelegram.devenirchef.data.User;
import antitelegram.devenirchef.utils.Constants;
import antitelegram.devenirchef.utils.Utils;

public class MainActivity extends DrawerBaseActivity {

    private static final int RC_SIGN_IN = 123;

    private RecyclerView recipesLayout;


    private List<Recipe> recipes;
    private List<String> selectedTags = new ArrayList<>();
    private int selectedComplexity = 1;
    private ChildEventListener childEventListener;
    private RelativeLayout bottomMenuExpanded;
    private RelativeLayout bottomMenuShrinked;
    private User user;

    private LinearLayout complexity;

    private boolean bottomExpanded = false;
    private RecipesAdapter recipesAdapter;
    private ViewGroup tagsContainer;

    private SearchView searchView;
    private ValueEventListener getUser;

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
        List<Recipe> newDataset = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getLevel() == selectedComplexity &&
                    (recipe.getTags().containsAll(selectedTags) || selectedTags.size() == 0)) {
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

    private View.OnClickListener getRateButtonListener(final int rateNumber) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("rateClick", "Clicked " + rateNumber);

                for (int i = 0; i <= rateNumber; ++i) {
                    ImageView tmpImageView = (ImageView) complexity.getChildAt(i);
                    tmpImageView.setImageResource(R.drawable.filled_star);
                }

                for (int i = rateNumber + 1; i < 5; ++i) {
                    ImageView tmpImageView = (ImageView) complexity.getChildAt(i);
                    tmpImageView.setImageResource(R.drawable.unfilled_star);
                }

                selectedComplexity = rateNumber + 1;
                selectRecipesComplexityAndTags();
            }
        };
    }

    private void setUpComplexitySeekbar() {
        complexity = bottomMenuExpanded.findViewById(R.id.rate_stars);
        for (int i = 0; i < 5; ++i) {
            complexity.getChildAt(i).setOnClickListener(getRateButtonListener(i));
        }
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
                    selectRecipesComplexityAndTags();
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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
        String queryTrimmed = query.trim();
        List<Recipe> res = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getTitle().toLowerCase().contains(queryTrimmed)) {
                res.add(recipe);
            } else {
                for (String tag : recipe.getTags()) {
                    if (tag.toLowerCase().contains(queryTrimmed)) {
                        res.add(recipe);
                        break;
                    }
                }
            }
        }

        return res;
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeAllRecipes();
    }

    void addDatabaseReadListener() {

        if (user == null) {
            final FirebaseUser currentUser = Utils.getFirebaseAuth().getCurrentUser();
            if (currentUser == null) {
                return;
            }
            initUserFromDatabase(currentUser);
            return;
        }

        attachRecipesListener();

    }

    private void initUserFromDatabase(FirebaseUser currentUser) {
        final DatabaseReference child = Utils.getFirebaseDatabase()
                .getReference(Constants.DATABASE_USERS)
                .child(currentUser.getUid());

        final ValueEventListener getUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    user = new User();
                    child.setValue(user);
                }
                attachRecipesListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        child.addListenerForSingleValueEvent(getUser);
    }

    private void attachRecipesListener() {
        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {

                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    Recipe newRecipe = dataSnapshot.getValue(Recipe.class);
                    if (newRecipe == null || user == null || newRecipe.getLevel() > user.getLevel())
                        return;
                    recipes.add(newRecipe);
                    recipesAdapter.changeDataset(recipes);
                    searchView.setQuery(searchView.getQuery().toString(), true);
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
        Utils.getFirebaseDatabase().getReference(Constants.DATABASE_RECIPES).addChildEventListener(childEventListener);
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
