package nyx;

import dobby.util.logging.Logger;
import nyx.goals.BuildGoal;
import nyx.goals.GoalResult;

public class Main {
    private static final Logger LOGGER = new Logger(Main.class, true);

    public static void main(String[] args) {
        final GoalResult result = new BuildGoal().execute();

        if (result == GoalResult.SUCCESS) {
            LOGGER.info("Build successful!");
        } else {
            LOGGER.error("Build failed!");
        }
    }
}