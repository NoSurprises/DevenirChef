package antitelegram.devenirchef;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class FinishRecipeStepFragment extends Fragment {


    private Button takePhoto;

    public FinishRecipeStepFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View finishScreen = inflater.inflate(R.layout.fragment_finish_recipe_step, container, false);

        initializeViews(finishScreen);
        setTakePhotoClickListener();

        return finishScreen;
    }

    private void setTakePhotoClickListener() {
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 1/7/2018 missed step of taking photo
                Activity cookingActivity = getActivity();
                cookingActivity.setResult(Activity.RESULT_OK);
                cookingActivity.finish();
            }
        });
    }

    private void initializeViews(View finishScreen) {
        takePhoto = finishScreen.findViewById(R.id.finish_take_photo);
    }

}
