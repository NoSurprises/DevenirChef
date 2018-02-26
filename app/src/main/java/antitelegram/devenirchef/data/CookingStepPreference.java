package antitelegram.devenirchef.data;

import android.content.SharedPreferences;

import java.util.Map;

public class CookingStepPreference implements SharedPreferencesModel {
    private SharedPreferences sharedPreferences;

    public CookingStepPreference(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void saveStep(String title, int currentItem) {
        sharedPreferences.edit().putInt(title, currentItem).apply();
    }

    @Override
    public int getSavedStep(String title) {
        return sharedPreferences.getInt(title, 0);
    }

    @Override
    public void removeSavedState(String title) {
        if (sharedPreferences.contains(title)) {
            sharedPreferences.edit().remove(title).apply();
        }
    }

    @Override
    public Map<String, ?> all() {
        return sharedPreferences.getAll();
    }
}
