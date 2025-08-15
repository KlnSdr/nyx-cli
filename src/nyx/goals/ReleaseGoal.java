package nyx.goals;

import common.logger.Logger;
import nyx.config.ProjectConfig;
import nyx.util.Git;
import nyx.util.ProjectHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ReleaseGoal implements Goal {
    private static final Logger LOGGER = new Logger(ReleaseGoal.class, true);

    @Override
    public String getHelp() {
        return "Prepares the project for release by bumping up the version.";
    }

    @Override
    public String getName() {
        return "release";
    }

    @Override
    public GoalResult execute() {
        final ProjectConfig config = ProjectHelper.getProjectConfig();

        if (config == null) {
            LOGGER.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        final String currentVersion = config.getProjectVersion();
        final String nextVersion = getNextVersion(currentVersion);

        if (nextVersion == null) {
            LOGGER.error("Failed to determine next version from current version: " + currentVersion);
            return GoalResult.FAILURE;
        }

        LOGGER.info("Current version: " + currentVersion);
        LOGGER.info("Next version: " + nextVersion);

        config.setProjectVersion(nextVersion);

        try (final PrintWriter writer = new PrintWriter("nyx.json", StandardCharsets.UTF_8)) {
            writer.print(config.toJson());
        } catch (IOException e) {
            LOGGER.error("Failed to write to project config file");
            LOGGER.trace(e);
            return GoalResult.FAILURE;
        }

        final boolean didCommit = new Git()
                .add("nyx.json")
                .commit("[prepare for release] bump version to " + nextVersion)
                .tag("v" + nextVersion)
                .execute();

        if (!didCommit) {
            LOGGER.error("Failed to commit changes to git");
            return GoalResult.FAILURE;
        }

        return GoalResult.SUCCESS;
    }

    private String getNextVersion(String currentVersion) {
        if (!currentVersion.endsWith("-snapshot")) {
            LOGGER.warn("Current version is not a snapshot: " + currentVersion);
            return null;
        }

        final String[] parts = currentVersion.replace("-snapshot", "").split("\\.");
        if (parts.length < 2) {
            LOGGER.error("Invalid version format: " + currentVersion);
            return null;
        }

        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);

        return String.format("%d.%d", major, minor);
    }
}
