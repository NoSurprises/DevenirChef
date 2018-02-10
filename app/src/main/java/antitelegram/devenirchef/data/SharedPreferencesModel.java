package antitelegram.devenirchef.data;


import java.util.Map;

public interface SharedPreferencesModel {

    /**
     * Save key-value pair to shared preferences
     *
     * @param title       key
     * @param currentItem value
     */
    void saveStep(String title, int currentItem);

    /**
     * Get the value from shared preferences by the key.
     * If nothing saved, then return 0
     *
     * @param title key
     * @return saved step number or 0 if nothing was saved
     */
    int getSavedStep(String title);

    /**
     * Remove any information stored with the key
     *
     * @param title key
     */
    void removeSavedState(String title);

    Map<String, ?> all();
}

