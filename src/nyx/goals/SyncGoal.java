package nyx.goals;

import dobby.util.json.NewJson;
import dobby.util.logging.Logger;
import nyx.util.ProjectHelper;
import nyx.util.RepoHelper;

import java.util.List;
import java.util.stream.Collectors;

public class SyncGoal implements Goal {
    private static final Logger LOGGER = new Logger(SyncGoal.class, true);

    @Override
    public GoalResult execute() {
        System.out.println("Syncing with remote repository...");
        final NewJson projectConfig = ProjectHelper.getProjectConfig();

        if (projectConfig == null) {
            return GoalResult.FAILURE;
        }

        LOGGER.info("syncing dependencies for project: " + projectConfig.getString("project.name"));

        final List<NewJson> dependencies = projectConfig.getList("project.dependencies").stream().map(o -> (NewJson) o).collect(Collectors.toList());

        for (NewJson dependency : dependencies) {
            final String group = dependency.getString("group");
            final String name = dependency.getString("name");
            final String version = dependency.getString("version");
            LOGGER.info("syncing dependency: " + group + ":" + name + ":" + version);

            if (!RepoHelper.existsInRepo(group, name, version)) {
                final boolean success = RepoHelper.downloadToRepo(group, name, version);

                if (!success) {
                    LOGGER.error("Failed to sync dependency: " + group + ":" + name + ":" + version);
                    return GoalResult.FAILURE;
                }
            }
        }

        return GoalResult.SUCCESS;
    }
}
