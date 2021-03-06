package antitelegram.devenirchef;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import antitelegram.devenirchef.data.FinishedRecipe;
import antitelegram.devenirchef.data.User;
import antitelegram.devenirchef.utils.Constants;
import antitelegram.devenirchef.utils.PhotoRedactor;
import antitelegram.devenirchef.utils.Utils;

public class UserInfoActivity extends DrawerBaseActivity {


    public static final int SHOW_RECIPES_LAST = 7;
    private static final int PICK_IMAGE = 321;
    private TextView username;
    private TextView userLevel;
    private TextView experience;
    private LinearLayout finishedRecipes;
    private FirebaseUser currentUser;
    private LayoutInflater layoutInflater;
    private ImageView userAvatar;
    private ProgressBar expBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_user_info);
        bindViews();
        setChangeImageListener();

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        layoutInflater = getLayoutInflater();
        currentUser = Utils.getFirebaseAuth().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        setUserInfoFromReference(getUserReference());
    }

    private void setChangeImageListener() {
        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(intent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            final Uri chosenImage = data.getData();

            try {
                changeImage(chosenImage);
            } catch (IOException e) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void changeImage(Uri chosenImage) throws IOException {


        Bitmap userImage = getFinishedImage(chosenImage); // it's rotated, if needed todo not working

        final StorageReference userAvatars = Utils.getFirebaseStorage().getReference("userAvatars/").child(currentUser.getUid());
        UploadTask upload = getUploadImageTask(userImage, userAvatars);

        upload.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                final Uri downloadUrl = task.getResult().getDownloadUrl();
                updateFirebaseUserImage(downloadUrl);
            }
        });

        userAvatar.setImageBitmap(userImage);

    }

    private Bitmap getFinishedImage(Uri chosenImage) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(chosenImage);
        Bitmap userImage = BitmapFactory.decodeStream(inputStream);

        userImage = redactPhoto(inputStream, userImage);

        inputStream.close();
        return userImage;
    }

    private Bitmap redactPhoto(InputStream inputStream, Bitmap userImage) {
        PhotoRedactor redactor = new PhotoRedactor();
        userImage = redactor.getRotatedPhoto(inputStream, userImage);
        return userImage;
    }

    private void updateFirebaseUserImage(Uri downloadUrl) {
        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUrl)
                .build();

        currentUser.updateProfile(changeRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
    }

    @NonNull
    private UploadTask getUploadImageTask(Bitmap userImage, StorageReference userAvatars) throws IOException {
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        userImage.compress(Bitmap.CompressFormat.JPEG, 20, imageBytes);
        UploadTask upload = userAvatars.putBytes(imageBytes.toByteArray());
        imageBytes.close();
        return upload;
    }

    private void setUserInfoFromReference(final DatabaseReference userReference) {
        setName(currentUser.getDisplayName());
        setUserImage();

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            User user;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                    return;
                }
                user = dataSnapshot.getValue(User.class);

                createFinishedRecipesViews();
                setLevel((long) user.getLevel());
                setExperience((long) user.getExp(), user.getLevel());
            }

            private void createFinishedRecipesViews() {
                List<FinishedRecipe> userFinishedRecipes = user.getFinishedRecipes();

                if (userFinishedRecipes == null) {
                    return;
                }

                final int n = userFinishedRecipes.size();
                for (int i = n - 1; i >= n - SHOW_RECIPES_LAST && i >= 0; i--) {
                    addFinishedRecipe(userFinishedRecipes.get(i));
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void setUserImage() {
        if (!isFinishing()) {
            Uri avatar = currentUser.getPhotoUrl();
            Glide.with(this)
                    .load(avatar)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(userAvatar);
        }
    }

    private DatabaseReference getUserReference() {
        String userUid = currentUser.getUid();
        return Utils.getFirebaseDatabase()
                .getReference(Constants.DATABASE_USERS)
                .child(userUid);
    }

    private void bindViews() {
        username = findViewById(R.id.username);
        userLevel = findViewById(R.id.userLevel);
        experience = findViewById(R.id.experience);
        finishedRecipes = findViewById(R.id.finished_recipes_container);
        userAvatar = findViewById(R.id.user_avatar);
        toolbar = findViewById(R.id.toolbar);
        expBar = findViewById(R.id.expBar);
    }

    private void setName(String name) {
        username.setText(name);
    }

    private void setLevel(Long level) {
        userLevel.setText(level.toString());
    }

    private void setExperience(Long exp, int level) {
        int progress = exp.intValue() - (int) Constants.EXP_LEVELS[level - 1];
        experience.setText(progress + "");
        expBar.setProgress(progress);
        expBar.setMax((int) Constants.EXP_LEVELS[level]);


    }

    private void addFinishedRecipe(FinishedRecipe recipe) {
        View finishedRecipe = layoutInflater.inflate(R.layout.user_info_recipe, finishedRecipes, false);
        setInfoToView(recipe, finishedRecipe);
        finishedRecipes.addView(finishedRecipe);
    }

    private void setInfoToView(final FinishedRecipe recipe, View finishedRecipe) {
        TextView title = finishedRecipe.findViewById(R.id.title);
        TextView rating = finishedRecipe.findViewById(R.id.rating);
        final ImageView image = finishedRecipe.findViewById(R.id.finished_image);

        setImage(recipe, image);
        title.setText(recipe.getTitle());
        rating.setText(getString(R.string.rating_1) +
            String.format("%.2f", recipe.getAverageRating()));
    }

    private void setImage(final FinishedRecipe recipe, final ImageView image) {

        String photoUrl = recipe.getPhotoUrl();
        if (photoUrl.equals(Constants.NO_FILE_ADDED))
            return;

        StorageReference imageRef = Utils.getFirebaseStorage().getReference(photoUrl);
        Task<Uri> imageTask = imageRef.getDownloadUrl();

        imageTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (!isFinishing()) {
                    Glide.with(getApplicationContext())
                            .load(uri)
                            .placeholder(R.mipmap.ic_launcher)
                            .crossFade()
                            .into(image);

                }
            }
        });
    }

    @Override
    void addDatabaseReadListener() {
    }
}
