package nyx.goals;

import java.util.List;

public class HelpGoal implements Goal {
    // TODO use discoverer
    private static final List<Goal> goals = List.of(
            new BuildGoal(),
            new CleanGoal(),
            new DebugGoal(),
            new DependeciesGoal(),
            new HelpGoal(),
            new InitGoal(),
            new InstallGoal(),
            new LoginGoal(),
            new PushGoal(),
            new ReleaseGoal(),
            new RunGoal(),
            new SnapshotGoal(),
            new SyncGoal(),
            new TestGoal(),
            new VersionGoal()
    );

    @Override
    public String getHelp() {
        return "Prints this help message";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public GoalResult execute() {
        int maxLength = goals.stream().mapToInt(g -> g.getName().length()).max().orElse(0);
        for (Goal goal : goals) {
            System.out.println(goal.getName() + " ".repeat(maxLength - goal.getName().length()) + "\t" + goal.getHelp());
        }

        return GoalResult.SUCCESS;
    }
}
