package antitelegram.devenirchef.data;

import android.os.Parcel;
import android.os.Parcelable;

public class RecipeStep implements Parcelable {
    static final Creator<RecipeStep> CREATOR = new Creator<RecipeStep>() {
        @Override
        public RecipeStep createFromParcel(Parcel in) {
            return new RecipeStep(in);
        }

        @Override
        public RecipeStep[] newArray(int size) {
            return new RecipeStep[size];
        }
    };
    private int stepNumber;
    private String descriptionOfStep;
    private String photoUrl;

    public RecipeStep() {

    }

    private RecipeStep(Parcel in) {
        stepNumber = in.readInt();
        descriptionOfStep = in.readString();
        photoUrl = in.readString();
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int value) {
        stepNumber = value;
    }

    // TODO: 12/30/2017 add image

    public String getDescriptionOfStep() {
        return descriptionOfStep;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(stepNumber);
        dest.writeString(descriptionOfStep);
        dest.writeString(photoUrl);
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

}
