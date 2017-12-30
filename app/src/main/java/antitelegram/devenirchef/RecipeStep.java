package antitelegram.devenirchef;

/**
 * Created by Nick on 12/30/2017.
 */

public class RecipeStep {
    private int stepNumber;
    private String descriptionOfStep;

    public RecipeStep() {
        this.stepNumber = 4;
        this.descriptionOfStep = "Sample";
    }

    public int getStepNumber() {
        return stepNumber;
    }
    // TODO: 12/30/2017 add image

    public String getDescriptionOfStep() {
        return descriptionOfStep;
    }
}
