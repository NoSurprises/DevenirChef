package antitelegram.devenirchef.cooking;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import antitelegram.devenirchef.R;
import antitelegram.devenirchef.data.RecipeStep;


public class RecipeCookingStepFragment extends Fragment {


    private static final String TAG = "daywint";
    private static final String RECIPE_KEY = "recipe";
    private TextView stepText;
    private RecipeStep recipeStep;
    private String text;

    public RecipeCookingStepFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: creating view");
        // Inflate the layout for this fragment
        View cookingStep = inflater.inflate(R.layout.cook_step, container, false);

        initializeViewFields(cookingStep);
        if (recipeStep == null) {
            recipeStep = savedInstanceState.getParcelable(RECIPE_KEY);
        }
        bindCookingStepDataToViews();
        Log.d(TAG, "onCreateView: binding data finished");
        return cookingStep;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(RECIPE_KEY, recipeStep);
    }

    private void initializeViewFields(View cookingStep) {
        stepText = cookingStep.findViewById(R.id.cooking_step_text);
        // TODO: 1/7/2018 initialize other fields. same as bind

    }

    public void setRecipeStep(RecipeStep step) {
        recipeStep = step;
    }


    private void bindCookingStepDataToViews() {
        stepText.setText(recipeStep.getDescriptionOfStep());
        // TODO: 1/7/2018 bind other data
    }




}
