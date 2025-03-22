package nyx;

import dobby.util.logging.Logger;
import nyx.goals.GoalResult;
import nyx.goals.InstallGoal;

public class Main {
    private static final Logger LOGGER = new Logger(Main.class, true);

    public static void main(String[] args) {
        final GoalResult result = new InstallGoal().execute();

        if (result == GoalResult.SUCCESS) {
            LOGGER.info("Build successful!");
        } else {
            LOGGER.error("Build failed!");
        }
    }
}