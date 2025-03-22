package nyx.goals;

import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import nyx.util.ProjectHelper;

import java.io.File;

import static nyx.util.ProcessHelper.executeCommand;

public class RunGoal implements Goal {
    private static final Logger logger = new Logger(RunGoal.class, true);

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
        final NewJson config = ProjectHelper.getProjectConfig();

        if (config == null) {
            logger.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        final String jarName = config.getString("project.name") + "-" + config.getString("project.version") + ".jar";
        final String jarPath = ProjectHelper.getProjectDir() + "/build/" + jarName;

        if (!new File(jarPath).exists()) {
            logger.error("Jar file does not exist. Run the build goal first.");
            return GoalResult.FAILURE;
        }

        final String runCommand = String.format("java -jar %s", jarPath);

        executeCommand(runCommand, true);

        return GoalResult.SUCCESS;
    }
}
