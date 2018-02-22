package antitelegram.devenirchef;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import antitelegram.devenirchef.data.FinishedRecipe;
import antitelegram.devenirchef.utils.Constants;
import antitelegram.devenirchef.utils.Utils;

import static antitelegram.devenirchef.MainActivity.TAG;

public class RateOthersActivity extends DrawerBaseActivity {

  private FirebaseUser currentUser;
  private Query query;
  private ValueEventListener refresher;

  private ImageView recipeImageBox;
  private Button nextButton;
  private Button rate1Button;
  private Button rate2Button;
  private Button rate3Button;
  private Button rate4Button;
  private Button rate5Button;
  private LinearLayout rateButtons;

  private List<FinishedRecipe> recipeList;
  private List<String> usersId;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentLayout(R.layout.rate_others);
    bindViews();
    bindButtons();

    currentUser = Utils.getFirebaseAuth().getCurrentUser();
    if (currentUser == null) {
      return;
    }
    String userUid = currentUser.getUid();
    final DatabaseReference currentUserReference = Utils.getFirebaseDatabase()
        .getReference(Constants.DATABASE_USERS)
        .child(userUid);

    recipeList = new ArrayList<>();
    usersId = new ArrayList<>();
    query = Utils.getFirebaseDatabase().getReference().child(Constants.DATABASE_USERS);

    refreshRecipes();

    Log.d("rateOthers", "Recipe list size " + recipeList.size());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    recipeList.clear();
    usersId.clear();
  }

  private void bindViews() {
    recipeImageBox = findViewById(R.id.RecipeImageBox);
    nextButton = findViewById(R.id.NextButton);
    rate1Button = findViewById(R.id.Rate1);
    rate2Button = findViewById(R.id.Rate2);
    rate3Button = findViewById(R.id.Rate3);
    rate4Button = findViewById(R.id.Rate4);
    rate5Button = findViewById(R.id.Rate5);
    rateButtons = findViewById(R.id.RateButtons);
  }

  private View.OnClickListener getRateButtonListener(final int rateNumber) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d("rateOthers", "rate button pressed");

        String user = usersId.get(0);

         final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USERS)
            .child(user);

        userRef.child("exp").addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d("rateOthers", "exp up");
            userRef.setValue((long) dataSnapshot.getValue() + rateNumber);
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
        });
      }
    };
  }

  private void bindButtons() {
    for (int i = 0; i < 5; ++i) {
      rateButtons.getChildAt(i).setOnClickListener(getRateButtonListener(i + 1));
    }

    nextButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        nextImage();
      }
    });
  }

  private void nextImage() {
    recipeList.remove(0);
    usersId.remove(0);

    Log.d("rateOthers", "Recipe list size " + recipeList.size());

    if (recipeList.isEmpty()) {
      refreshRecipes();
    }
    else {
      setImage(recipeList.get(0), recipeImageBox);
    }

    // TODO: Do something if there are no recipes left
  }

  private void refreshRecipes() {
    refresher = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if (!recipeList.isEmpty())
          return;

        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

          String user = userSnapshot.getKey();

          // TODO: Uncomment this
          //if (user.equals(currentUser.getUid()))
           // continue;

          for (DataSnapshot recipeSnapshot : userSnapshot.child("finishedRecipes").getChildren()) {
            FinishedRecipe recipe = recipeSnapshot.getValue(FinishedRecipe.class);
            //Log.d("alex", "onDataChange: " + recipe.getPhotoUrl());
            if (!recipe.getPhotoUrl().equals("none")) {
              recipeList.add(recipe);
              usersId.add(user);

              Log.d("rateOthers", "onDataChangeIf: " + recipe.getPhotoUrl());
            }
          }
        }
        Log.d("rateOthers", "Recipe list size " + recipeList.size());
        setImage(recipeList.get(0), recipeImageBox);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    };
    query.addValueEventListener(refresher);
  }

  private void setImage(final FinishedRecipe recipe, final ImageView image) {

    image.setImageResource(R.drawable.ic_placeholder_transparent);
    Log.d(TAG, "setImage: " + recipe.getTitle());

    String photoUrl = recipe.getPhotoUrl();
    if (photoUrl.equals(Constants.NO_FILE_ADDED))
      return;

    StorageReference imageRef = Utils.getFirebaseStorage().getReference(photoUrl);
    Task<Uri> imageTask = imageRef.getDownloadUrl();
    Log.d(TAG, "setImage: start getting url from firebase");
    imageTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
      @Override
      public void onSuccess(Uri uri) {
          Log.d(TAG, "start glide downloading ");
        if (!isFinishing()) {

          Glide.with(image.getContext())
              .load(uri)
                  .error(R.drawable.ic_search_black_24dp)
                  .placeholder(R.drawable.ic_placeholder_transparent)
                  .dontAnimate()
              .into(image);

        }
      }
    });
  }

  @Override
  void addDatabaseReadListener() {

  }
}
