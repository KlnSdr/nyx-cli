package nyx.goals;

import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import nyx.util.ProjectHelper;

import java.util.List;

public class SyncGoal implements Goal {
    private static final Logger LOGGER = new Logger(SyncGoal.class);

    @Override
    public GoalResult execute() {
        System.out.println("Syncing with remote repository...");
        final NewJson projectConfig = ProjectHelper.getProjectConfig();

        if (projectConfig == null) {
            return GoalResult.FAILURE;
        }

        LOGGER.info("syncing dependencies for project: " + projectConfig.getString("project.name"));

        final List<NewJson> dependencies = projectConfig.getList("project.dependencies").stream().map(o -> (NewJson) o).toList();

        for (NewJson dependency : dependencies) {
            LOGGER.info("syncing dependency: " +
                    dependency.getString("group") + "." +
                    dependency.getString("name") + ":" +
                    dependency.getString("version")
            );
        }

        return GoalResult.SUCCESS;
    }
}
