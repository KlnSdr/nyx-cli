package nyx.goals;

import dobby.util.logging.LogLevel;
import dobby.util.logging.Logger;

public class DebugGoal implements Goal {
    @Override
    public GoalResult execute() {
        Logger.setMaxLogLevel(LogLevel.DEBUG);
        return GoalResult.SUCCESS;
    }
}
