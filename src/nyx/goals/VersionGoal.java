package nyx.goals;

import nyx.Main;

public class VersionGoal implements Goal {
    @Override
    public GoalResult execute() {
        System.out.println("nyx version " + Main.VERSION);
        return GoalResult.SUCCESS;
    }
}
