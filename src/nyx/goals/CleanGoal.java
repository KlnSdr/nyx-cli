package nyx.goals;

import dobby.util.logging.Logger;

import java.io.File;

import static nyx.util.ProjectHelper.getProjectDir;

public class CleanGoal implements Goal {
    private static final Logger LOGGER = new Logger(CleanGoal.class, true);

    @Override
    public GoalResult execute() {
        final String projectDir = getProjectDir();
        LOGGER.info(String.format("Cleaning up %s", projectDir));

        final File buildDir = new File(projectDir + "/build");
        if (buildDir.exists()) {
            final boolean success = deleteDirectory(buildDir);
            if (!success) {
                LOGGER.error("Failed to delete build directory");
                return GoalResult.FAILURE;
            }
        }

        return GoalResult.SUCCESS;
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
