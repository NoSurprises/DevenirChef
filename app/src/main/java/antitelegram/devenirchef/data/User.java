package antitelegram.devenirchef.data;

import java.util.List;

/**
 * Created by Nick on 1/16/2018.
 */

public class User {
    int level;
    int exp;
    private List<FinishedRecipe> finishedRecipes;

    public User() {
        level = 1;
    }

    public List<FinishedRecipe> getFinishedRecipes() {
        return finishedRecipes;
    }

    public void setFinishedRecipes(List<FinishedRecipe> finishedRecipes) {
        this.finishedRecipes = finishedRecipes;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }
}
