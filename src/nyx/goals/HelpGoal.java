package nyx.goals;

import java.util.List;

public class HelpGoal implements Goal {
    // TODO use discoverer
    private static final List<Goal> goals = List.of(
            new BuildGoal(),
            new CleanGoal(),
            new DebugGoal(),
            new HelpGoal(),
            new InstallGoal(),
            new InitGoal(),
            new RunGoal(),
            new BuildGoal(),
            new SyncGoal(),
            new VersionGoal()
    );

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public GoalResult execute() {
        for (Goal goal : goals) {
            System.out.println(goal.getName() + " - " + goal.getHelp());
        }

        return GoalResult.SUCCESS;
    }
}
