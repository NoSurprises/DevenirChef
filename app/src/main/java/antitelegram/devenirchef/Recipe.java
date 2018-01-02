package antitelegram.devenirchef;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 12/30/2017.
 */

public class Recipe {
    private String title;
    private String ingredients;
    private String description;
    private String photoUrl;
    private List<RecipeStep> cookingSteps;

    public Recipe() {
        this.title = "Sample";
        this.ingredients = "1. Sample 2.Sample";
        this.description = "sample sample text text sample sample text text";
        this.cookingSteps = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            cookingSteps.add(new RecipeStep());
        }
    }

    public String getTitle() {
        return title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getDescription() {
        return description;
    }

    public List<RecipeStep> getCookingSteps() {
        return cookingSteps;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
