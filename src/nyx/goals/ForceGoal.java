package nyx.goals;

public class ForceGoal implements Goal {
    private static boolean FORCE = false;

    @Override
    public String getHelp() {
        return "Sets the force flag to true, effects differ based on the goal. Some goals may not be affected by this flag.";
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

    public static boolean isForce() {
        return FORCE;
    }
}
