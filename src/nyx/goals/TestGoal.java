package nyx.goals;

import common.logger.Logger;
import nyx.config.ProjectConfig;
import nyx.util.ProjectHelper;

import java.io.File;

import static nyx.util.ProcessHelper.executeCommand;

public class TestGoal implements Goal {
    private static final Logger logger = new Logger(TestGoal.class, true);
    @Override
    public String getHelp() {
        return "Runs tests with the common.test framework";
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public GoalResult execute() {
        final ProjectConfig config = ProjectHelper.getProjectConfig();

        if (config == null) {
            logger.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        if (!checkIfTestFrameworkPresent(config)) {
            logger.info("No common.test dependency found. Skipping tests.");
            return GoalResult.SUCCESS;
        }

        final String jarName = config.getProjectName() + "-" + config.getProjectVersion() + ".jar";
        final String jarPath = ProjectHelper.getProjectDir() + "/build/" + jarName;

        if (!new File(jarPath).exists()) {
            logger.warn("Jar file not found: " + jarPath);
            logger.info("Running 'build' goal to create the jar file.");
            final GoalResult buildResult = new BuildGoal().execute();
            if (buildResult == GoalResult.FAILURE) {
                return GoalResult.FAILURE;
            }
        }

        final String testCommand = String.format("java -cp %s common.test.TestRunner", jarPath);
        return executeCommand(testCommand, true) ? GoalResult.SUCCESS : GoalResult.FAILURE;
    }

    private boolean checkIfTestFrameworkPresent(ProjectConfig config) {
        return config.getDependencies().stream().anyMatch(dep ->
            dep.getGroup().equals("common") && dep.getName().equals("test")
        );
    }
}
