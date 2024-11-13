package nyx;

import nyx.goals.GoalResult;
import nyx.goals.SyncGoal;

public class Main {
    public static void main(String[] args) {
        final GoalResult result = new SyncGoal().execute();

        if (result == GoalResult.SUCCESS) {
            System.out.println("Sync successful!");
        } else {
            System.out.println("Sync failed!");
        }
    }
}