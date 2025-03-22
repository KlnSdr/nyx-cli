package nyx.goals;

import dobby.util.logging.Logger;

public class RunGoal implements Goal {
    @Override
    public String getHelp() {
        return "Runs the artifact";
    }

    @Override
    public String getName() {
        return "run";
    }

    @Override
    public GoalResult execute() {
        new Logger(RunGoal.class, true).warn("run not yet implemented");
        return GoalResult.FAILURE;
    }
}
