package antitelegram.devenirchef;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class RecipeCookingStepFragment extends Fragment {


    private static final String TAG = "daywint";
    private TextView stepText;
    private RecipeStep recipeStep;

    public RecipeCookingStepFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View cookingStep = inflater.inflate(R.layout.cook_step, container, false);

        initializeViewFields(cookingStep);
        bindCookingStepDataToViews();

        return cookingStep;
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
