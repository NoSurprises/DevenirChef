package antitelegram.devenirchef.cooking;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import antitelegram.devenirchef.R;
import antitelegram.devenirchef.data.RecipeStep;


public class RecipeCookingStepFragment extends Fragment {


    private static final String RECIPE_KEY = "recipe";
    private TextView stepText;
    private RecipeStep recipeStep;
    private TextView stepNumber;
    private Button forward;
    private Button back;
    private ImageView image;

    public RecipeCookingStepFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View cookingStep = inflater.inflate(R.layout.cook_step, container, false);

        initializeViewFields(cookingStep);
        if (recipeStep == null) {
            recipeStep = savedInstanceState.getParcelable(RECIPE_KEY);
        }
        bindCookingStepDataToViews();
        handleNavigationButtons();

        return cookingStep;
    }

    private void handleNavigationButtons() {
        final StepsNavigation navigation = ((StepsNavigation) getActivity());

        setBackNavigation(navigation);
        setForwardNavigation(navigation);
    }

    private void setForwardNavigation(final StepsNavigation navigation) {
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigation.forward();
            }
        });
    }

    private void setBackNavigation(final StepsNavigation navigation) {
        if (recipeStep.getStepNumber() == 0) {
            back.setEnabled(false);
        } else {
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navigation.back();
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(RECIPE_KEY, recipeStep);
    }

    private void initializeViewFields(View cookingStep) {
        stepText = cookingStep.findViewById(R.id.cooking_step_text);
        stepNumber = cookingStep.findViewById(R.id.step_number);
        forward = cookingStep.findViewById(R.id.forward_button);
        back = cookingStep.findViewById(R.id.back_button);
        image = cookingStep.findViewById(R.id.step_image);

    }

    public void setRecipeStep(RecipeStep step) {
        recipeStep = step;
    }


    private void bindCookingStepDataToViews() {
        stepText.setText(recipeStep.getDescriptionOfStep());
        stepNumber.setText(String.valueOf(recipeStep.getStepNumber() + 1));
        Log.d("daywint", "bindCookingStepDataToViews: photo " + recipeStep.getPhotoUrl());
        if (!getActivity().isFinishing() && recipeStep.getPhotoUrl() != null) {
            Glide.with(getActivity())
                    .load(recipeStep.getPhotoUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .into(image);
        }

    }


}
