package nyx.goals;

import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import nyx.util.ProjectHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class InitGoal implements Goal {
    private static final Logger LOGGER = new Logger(InitGoal.class, true);

    @Override
    public String getHelp() {
        return "Initializes a new Nyx project";
    }

    @Override
    public String getName() {
        return "init";
    }

    @Override
    public GoalResult execute() {
        final NewJson config = ProjectHelper.emptyConfig();

        final File configFile = new File("nyx.json");

        try {
            final boolean created = configFile.createNewFile();

            if (!created) {
                LOGGER.error("Project already initialized");
                return GoalResult.FAILURE;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create project config file");
            LOGGER.trace(e);
            return GoalResult.FAILURE;
        }

        try {
            final PrintWriter writer = new PrintWriter("nyx.json", StandardCharsets.UTF_8);
            writer.print(config);
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Failed to write to project config file");
            LOGGER.trace(e);
            return GoalResult.FAILURE;
        }

        LOGGER.info("Project initialized");

        return GoalResult.SUCCESS;
    }
}
