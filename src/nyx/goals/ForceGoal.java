package nyx.goals;

public class ForceGoal implements Goal {
    private static boolean FORCE = false;

    @Override
    public String getHelp() {
        return "Sets the force flag to true, effects differ based on the goal. Some goals may not be affected by this flag. Flag resets after use by a goal.";
    }

    @Override
    public String getName() {
        return "force";
    }

    @Override
    public GoalResult execute() {
        FORCE = true;
        return GoalResult.SUCCESS;
    }

    /// Returns true if the force flag is set, and resets it to false.
    public static boolean isForce() {
        final boolean value = FORCE;
        FORCE = false; // reset after use
        return value;
    }
}
