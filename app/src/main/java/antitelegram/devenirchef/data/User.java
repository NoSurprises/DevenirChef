package antitelegram.devenirchef.data;

import java.util.List;



public class User {
    private int level;
    private int exp;
    private List<FinishedRecipe> finishedRecipes;

    public User() {
        level = 1;
        exp = 0;
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
