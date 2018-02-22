package antitelegram.devenirchef.data;

import java.util.ArrayList;
import java.util.List;


public class FinishedRecipe {
    private String title;
    private String photoUrl;
    private List<String> usersRated;
    private int level;
    private int averageRating;

    public FinishedRecipe() {
        usersRated = new ArrayList<>();
        averageRating = 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }

    public int getAverageRating() { return averageRating; }

    public void setAverageRating(int averageRating) { this.averageRating = averageRating; }

    public List<String> getUsersRated() { return usersRated; }

    public void addUsersRated(String user) { usersRated.add(user); }
}
