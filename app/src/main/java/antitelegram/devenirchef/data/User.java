package antitelegram.devenirchef.data;

import java.util.ArrayList;

/**
 * Created by Nick on 1/16/2018.
 */

public class User {
    int level;
    int exp;
    private ArrayList<FinishedRecipe> finishedRecipes;

    public User() {
        this.finishedRecipes = new ArrayList<>();
        this.level = 1;
        this.exp = 0;
    }

    public ArrayList<FinishedRecipe> getFinishedRecipes() {
        return finishedRecipes;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }
}
