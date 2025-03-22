package nyx.goals;

import dobby.util.logging.Logger;

public class InitGoal implements Goal {
    @Override
    public GoalResult execute() {
        new Logger(InitGoal.class, true).warn("init not yet implemented");
        return GoalResult.FAILURE;
    }
}
