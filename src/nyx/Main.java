package nyx;

import common.logger.LogLevel;
import common.logger.Logger;
import nyx.goals.*;

import java.util.Arrays;

public class Main {
    private static final Logger LOGGER = new Logger(Main.class, true);
    public static final String VERSION = "${PROJECT_VERSION}";

    public static void main(String[] args) {
        Logger.setMaxLogLevel(LogLevel.INFO);

        for (String arg : args) {
            if (!isKnownGoal(arg.toLowerCase())) {
                LOGGER.error("Unknown goal: " + arg);
                System.exit(1);
            }
        }

        for (String arg : args) {
            final Goal goal = getGoal(arg);
            if (goal == null) {
                LOGGER.error("Unknown goal: " + arg);
                System.exit(1);
            }

            final GoalResult result = goal.execute();
            if (result == GoalResult.FAILURE) {
                LOGGER.error("Goal failed: " + arg);
                System.exit(1);
            }
        }
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
                "snapshot"};
        return Arrays.asList(knownGoals).contains(goal);
    }
}