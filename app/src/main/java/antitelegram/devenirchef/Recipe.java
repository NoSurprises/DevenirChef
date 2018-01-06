package antitelegram.devenirchef;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 12/30/2017.
 */

public class Recipe implements Parcelable {
    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };
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

    protected Recipe(Parcel in) {
        title = in.readString();
        ingredients = in.readString();
        description = in.readString();
        photoUrl = in.readString();
        cookingSteps = in.readArrayList(RecipeStep.class.getClassLoader());

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(ingredients);
        dest.writeString(description);
        dest.writeString(photoUrl);
        dest.writeList(cookingSteps);
    }
}
