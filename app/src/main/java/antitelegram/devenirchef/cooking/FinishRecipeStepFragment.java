package antitelegram.devenirchef.cooking;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

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
            finishImage = ((Bitmap) savedInstanceState.get("image"));
            imageView.setImageBitmap(finishImage);
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
                    cookingActivity.finish();
                }

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


    private void dispatchTakePictureIntent(Context context) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private void initializeViews(View finishScreen) {
        takePhoto = finishScreen.findViewById(R.id.finish_take_photo);
        mainScreen = finishScreen.findViewById(R.id.main_screen_button);
        imageView = finishScreen.findViewById(R.id.image_result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == FinishRecipeStepFragment.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            finishImage = (Bitmap) extras.get("data");
            imageView.setImageBitmap(finishImage);

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("image", finishImage);
    }
}

