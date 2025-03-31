package nyx.goals;

import common.logger.LogLevel;
import common.logger.Logger;

public class DebugGoal implements Goal {
    @Override
    public String getHelp() {
        return "Sets the log level to debug";
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public GoalResult execute() {
        Logger.setMaxLogLevel(LogLevel.DEBUG);
        return GoalResult.SUCCESS;
    }
}
