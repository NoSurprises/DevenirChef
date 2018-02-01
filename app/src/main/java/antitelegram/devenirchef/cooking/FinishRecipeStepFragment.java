package antitelegram.devenirchef.cooking;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import antitelegram.devenirchef.R;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FinishRecipeStepFragment extends Fragment {


    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "daywint";
    private Button takePhoto;
    private Button mainScreen;
    private ImageView imageView;
    private Bitmap finishImage;
    private String takenImagePath;
    private Button share;
    private Uri photoUri;

    public FinishRecipeStepFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View finishScreen = inflater.inflate(R.layout.fragment_finish_recipe_step, container, false);

        initializeViews(finishScreen);
        setButtonsListeners();

        if (savedInstanceState != null && savedInstanceState.containsKey("image")) {
            takenImagePath = savedInstanceState.getString("image");
            initImageBitmap();
            setPic(finishImage);
        }
        return finishScreen;
    }

    private void setButtonsListeners() {

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraHardware(v.getContext())) {
                    dispatchTakePictureIntent(v.getContext());

                }
            }
        });

        mainScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CookActivity cookActivity = (CookActivity) getActivity();
                if (cookActivity != null) {
                    cookActivity.saveImageToDatabase(finishImage);
                }

                Activity cookingActivity = getActivity();
                if (cookingActivity != null) {
                    cookingActivity.setResult(RESULT_OK);
                    deleteTakenPhoto();
                    cookingActivity.finish();
                }

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareOverstagram();
            }
        });

        // todo remove debug listener
        mainScreen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                CookActivity cookActivity = (CookActivity) getActivity();
                if (cookActivity != null) {
                    cookActivity.removeUsersData();
                }

                return true;
            }
        });
    }

    private void deleteTakenPhoto() {
        if (takenImagePath != null) {
            new File(takenImagePath).delete();
        }
    }

    private void shareOverstagram() {
        if (!isInstagramInstalled()) {
            Toast.makeText(getActivity(), "Instagram not installed!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
        shareIntent.setPackage("com.instagram.android");
        startActivity(shareIntent);
    }

    private boolean isInstagramInstalled() {
        try {
            ApplicationInfo info = getActivity().getPackageManager().getApplicationInfo("com.instagram.android", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void dispatchTakePictureIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            File photo = null;
            try {
                photo = createImageFile();
            } catch (IOException e) {
                Toast.makeText(context, "Can't create picture", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "dispatchTakePictureIntent: " + e);
            }
            if (photo != null) {
                photoUri = FileProvider.getUriForFile(getActivity(), "devenirchef.fileprovider", photo);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void initializeViews(View finishScreen) {
        takePhoto = finishScreen.findViewById(R.id.finish_take_photo);
        mainScreen = finishScreen.findViewById(R.id.main_screen_button);
        imageView = finishScreen.findViewById(R.id.image_result);
        share = finishScreen.findViewById(R.id.share);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FinishRecipeStepFragment.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            initImageBitmap();
            finishImage = getRotatedPhoto();

            if (finishImage != null) {
                setPic(finishImage);
            }
        }
    }

    private Bitmap getRotatedPhoto() {

        InputStream in = null;
        try {
            in = getActivity().getContentResolver().openInputStream(photoUri);

            int rotation = getImageRotation(new ExifInterface(in));
            return getRotatedBitmap(finishImage, rotation);
        } catch (IOException e) {
            Log.d(TAG, "onActivityResult:" + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private Bitmap getRotatedBitmap(Bitmap image, int rotation) {
        Matrix matrix = new Matrix();
        matrix.setRotate(rotation, (float) image.getWidth() / 2, (float) image.getHeight() / 2);
        return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    private int getImageRotation(ExifInterface exifInterface) {
        int rotation = 0;
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 270;
                break;
        }
        return rotation;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFilename = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFilename, ".jpg", storageDir);
        takenImagePath = image.getAbsolutePath();

        return image;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("image", takenImagePath);
    }

    private void setPic(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    private void initImageBitmap() {
        finishImage = BitmapFactory.decodeFile(takenImagePath);
    }
}

