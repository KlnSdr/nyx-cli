package nyx;

import common.logger.LogLevel;
import common.logger.Logger;
import nyx.goals.*;

import java.util.Arrays;
import java.util.Date;

public class Main {
    private static final Logger LOGGER = new Logger(Main.class, true);
    public static final String VERSION = "${PROJECT_VERSION}";

    public static void main(String[] args) {
        final Date start = new Date();
        boolean hadErrors = false;
        Logger.setMaxLogLevel(LogLevel.INFO);

        for (String arg : args) {
            if (!isKnownGoal(arg.toLowerCase())) {
                LOGGER.error("Unknown goal: " + arg);
                hadErrors = true;
                break;
            }
        }

        if (hadErrors) {
            printSummary(start, true);
        }

        for (String arg : args) {
            final Goal goal = getGoal(arg);
            LOGGER.info("");
            LOGGER.info("============");
            LOGGER.info(arg);
            LOGGER.info("============");
            LOGGER.info("");

            if (goal == null) {
                LOGGER.error("Unknown goal: " + arg);
                hadErrors = true;
                break;
            }

            final GoalResult result = goal.execute();
            LOGGER.info("");
            LOGGER.info("============");
            LOGGER.info("Finished " + arg + " with result: " + result);
            LOGGER.info("============");
            LOGGER.info("");

            if (result == GoalResult.FAILURE) {
                LOGGER.error("Goal failed: " + arg);
                hadErrors = true;
                break;
            }
        }

        printSummary(start, hadErrors);
    }

    private static Goal getGoal(String goal) {
        switch (goal) {
            case "sync":
                return new SyncGoal();
            case "build":
                return new BuildGoal();
            case "install":
                return new InstallGoal();
            case "push":
                return new PushGoal();
            case "clean":
                return new CleanGoal();
            case "run":
                return new RunGoal();
            case "version":
                return new VersionGoal();
            case "debug":
                return new DebugGoal();
            case "help":
                return new HelpGoal();
            case "init":
                return new InitGoal();
            case "dependencies":
                return new DependeciesGoal();
            case "login":
                return new LoginGoal();
            case "release":
                return new ReleaseGoal();
            case "snapshot":
                return new SnapshotGoal();
            case "test":
                return new TestGoal();
            case "force":
                return new ForceGoal();
            default:
                return null;
        }
    }

    private static boolean isKnownGoal(String goal) {
        final String[] knownGoals = {
                "sync",
                "build",
                "install",
                "push",
                "clean",
                "run",
                "version",
                "debug",
                "help",
                "init",
                "dependencies",
                "login",
                "release",
                "snapshot",
                "test",
                "force"
        };
        return Arrays.asList(knownGoals).contains(goal);
    }

    private static void printSummary(Date start, boolean hadErrors) {
        final Date end = new Date();
        final long duration = end.getTime() - start.getTime();
        if (hadErrors) {
            LOGGER.error("Build finished with errors.");
            LOGGER.error("Build duration: " + duration + " ms");
        } else {
            LOGGER.info("Build finished successfully.");
            LOGGER.info("All goals completed in " + duration + " ms");
        }
        System.exit(hadErrors ? 1 : 0);
    }
}