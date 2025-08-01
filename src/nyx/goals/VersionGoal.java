package nyx.goals;

import nyx.Main;

public class VersionGoal implements Goal {
    @Override
    public String getHelp() {
        return "Prints the version of Nyx";
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public GoalResult execute() {
        System.out.println("nyx version " + Main.VERSION);
        return GoalResult.SUCCESS;
    }
}
