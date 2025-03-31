package nyx.goals;

import common.logger.Logger;

public class PushGoal implements Goal {
    @Override
    public String getHelp() {
        return "Pushes the artifact to the remote repository";
    }

    @Override
    public String getName() {
        return "push";
    }

    @Override
    public GoalResult execute() {
        new Logger(PushGoal.class, true).warn("push not yet implemented");
        return GoalResult.FAILURE;
    }
}
