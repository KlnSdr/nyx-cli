package nyx.goals;

import common.logger.Logger;
import nyx.config.Dependency;
import nyx.config.ProjectConfig;
import nyx.util.ProjectHelper;

import java.util.List;

public class DependeciesGoal implements Goal {
    private static final Logger LOGGER = new Logger(DependeciesGoal.class, true);
    @Override
    public String getHelp() {
        return "Displays all dependencies of the current project (including transitive dependencies).";
    }

    @Override
    public String getName() {
        return "dependencies";
    }

    @Override
    public GoalResult execute() {
        if (new SyncGoal().execute() == GoalResult.FAILURE) {
            LOGGER.error("Failed to sync dependencies");
            return GoalResult.FAILURE;
        }

        final ProjectConfig config = ProjectHelper.getProjectConfig();

        if (config == null) {
            LOGGER.error("Failed to read project config");
            return GoalResult.FAILURE;
        }

        LOGGER.info(config.getProjectGroup() + ":" + config.getProjectName() + ":" + config.getProjectVersion());
        printDependencyTree(config, "");

        return GoalResult.SUCCESS;
    }

    private void printDependencyTree(ProjectConfig config, String prefix) {
        List<Dependency> dependencies = config.getDependencies();
        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dependency = dependencies.get(i);
            LOGGER.info(prefix + (i == dependencies.size() - 1 ? "\\" : "+") + "- " + dependency.getGroup() + ":" + dependency.getName() + ":" + dependency.getVersion());
            ProjectConfig depConfig = ProjectHelper.getConfigOfDependency(dependency.getGroup(), dependency.getName(), dependency.getVersion());
            if (depConfig != null) {
                printDependencyTree(depConfig, prefix + (i == dependencies.size() - 1 ? " " : "|") + "   ");
            }
        }
    }
}
