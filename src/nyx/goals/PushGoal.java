package nyx.goals;

import dobby.util.logging.Logger;

public class PushGoal implements Goal {
    @Override
    public GoalResult execute() {
        new Logger(PushGoal.class, true).warn("push not yet implemented");
        return GoalResult.FAILURE;
    }
}
