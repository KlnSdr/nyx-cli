package nyx.goals;

import dobby.util.logging.Logger;
import nyx.config.Dependency;
import nyx.config.ProjectConfig;
import nyx.util.ProjectHelper;
import nyx.util.RepoHelper;

import java.util.List;

public class SyncGoal implements Goal {
    private static final Logger LOGGER = new Logger(SyncGoal.class, true);

    @Override
    public String getHelp() {
        return "Downloads dependencies from the remote repository";
    }

    @Override
    public String getName() {
        return "sync";
    }

    @Override
    public GoalResult execute() {
        LOGGER.info("Syncing with remote repository...");
        final ProjectConfig projectConfig = ProjectHelper.getProjectConfig();

        if (projectConfig == null) {
            return GoalResult.FAILURE;
        }

        final RepoHelper repo = RepoHelper.getInstance(projectConfig.getRemoteRepoUrl());

        LOGGER.info("syncing dependencies for project: " + projectConfig.getProjectName());

        final List<Dependency> dependencies = projectConfig.getDependencies();

        for (Dependency dependency : dependencies) {
            final String group = dependency.getGroup();
            final String name = dependency.getName();
            final String version = dependency.getVersion();
            LOGGER.info("syncing dependency: " + group + ":" + name + ":" + version);

            if (!repo.existsInRepo(group, name, version)) {
                final boolean success = repo.downloadToRepo(group, name, version);

                if (!success) {
                    LOGGER.error("Failed to sync dependency: " + group + ":" + name + ":" + version);
                    return GoalResult.FAILURE;
                }
            }
        }

        return GoalResult.SUCCESS;
    }
}
