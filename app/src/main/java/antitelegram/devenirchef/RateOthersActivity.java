package antitelegram.devenirchef;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import antitelegram.devenirchef.data.FinishedRecipe;
import antitelegram.devenirchef.data.User;
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

    Log.d("alex", "Recipe list size " + recipeList.size());

    //setImage(recipeList.get(0), recipeImageBox);
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
  }

  private View.OnClickListener getRateButtonListener(final int rateNumber) {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.d("alex", "rate button pressed");

        String user = usersId.get(0);

        //final DatabaseReference ref = Utils.getFirebaseDatabase().getReference(Constants.DATABASE_USERS)
         final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_USERS)
            .child(user).child("exp");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d("alex", "exp up");
            ref.setValue((long) dataSnapshot.getValue() + rateNumber);
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
        });
      }
    };
  }

  private void bindButtons() {
    rate1Button.setOnClickListener(getRateButtonListener(1));
    rate2Button.setOnClickListener(getRateButtonListener(2));
    rate3Button.setOnClickListener(getRateButtonListener(3));
    rate4Button.setOnClickListener(getRateButtonListener(4));
    rate5Button.setOnClickListener(getRateButtonListener(5));


    nextButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        recipeList.remove(0);
        usersId.remove(0);

        Log.d("alex", "Recipe list size " + recipeList.size());

        if (recipeList.isEmpty()) {
          refreshRecipes();
        }
        else {
          setImage(recipeList.get(0), recipeImageBox);
        }

        // TODO: Do something if there are no recipes left
      }
    });
  }


  private void refreshRecipes() {
    //query.addListenerForSingleValueEvent(new ValueEventListener() {
    refresher = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Log.d("alex", "I SHOULD BE HERE");

        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

          //Log.d("alex", "User children " + userSnapshot.getChildrenCount());

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

              Log.d("alex", "onDataChangeIf: " + recipe.getPhotoUrl());
            }
          }
        }
        Log.d("alex", "Recipe list size " + recipeList.size());
        setImage(recipeList.get(0), recipeImageBox);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    };
    query.addListenerForSingleValueEvent(refresher);
  }

  private void setImage(final FinishedRecipe recipe, final ImageView image) {

    image.setImageResource(0);

    String photoUrl = recipe.getPhotoUrl();
    if (photoUrl.equals(Constants.NO_FILE_ADDED))
      return;

    StorageReference imageRef = Utils.getFirebaseStorage().getReference(photoUrl);
    Task<Uri> imageTask = imageRef.getDownloadUrl();

    imageTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
      @Override
      public void onSuccess(Uri uri) {
        if (!isFinishing()) {
          Glide.with(image.getContext())
              .load(uri)
              .into(image);

          Log.d(TAG, "setInfoToView: set image " + recipe.getPhotoUrl());
        }
      }
    });
  }


  @Override
  void addDatabaseReadListener() {

  }
}
