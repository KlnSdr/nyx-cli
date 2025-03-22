package nyx.goals;

public interface Goal {
    String getHelp();
    String getName();
    GoalResult execute();
}
