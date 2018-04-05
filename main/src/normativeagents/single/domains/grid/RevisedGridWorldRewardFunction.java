package normativeagents.single.domains.grid;

/**
 * Created by dkasenberg on 10/23/17.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.model.RewardFunction;

public class RevisedGridWorldRewardFunction implements RewardFunction {
    protected double[][] rewardMatrix;
    protected int width;
    protected int height;

    public RevisedGridWorldRewardFunction(int width, int height, double initializingReward) {
        this.initialize(width, height, initializingReward);
    }

    public RevisedGridWorldRewardFunction(int width, int height) {
        this(width, height, 0.0D);
    }

    protected void initialize(int width, int height, double initializingReward) {
        this.rewardMatrix = new double[width][height];
        this.width = width;
        this.height = height;

        for(int i = 0; i < this.width; ++i) {
            for(int j = 0; j < this.height; ++j) {
                this.rewardMatrix[i][j] = initializingReward;
            }
        }

    }

    public double[][] getRewardMatrix() {
        return this.rewardMatrix;
    }

    public void setReward(int x, int y, double r) {
        this.rewardMatrix[x][y] = r;
    }

    public double getRewardForTransitionsTo(int x, int y) {
        return this.rewardMatrix[x][y];
    }

    public double reward(State s, Action a, State sprime) {
        int x = StateUtilities.stringOrNumber(s.get(new OOVariableKey("agent", "x"))).intValue();
        int y = StateUtilities.stringOrNumber(s.get(new OOVariableKey("agent","y"))).intValue();
        if(x < this.width && x >= 0 && y < this.height && y >= 0) {
            double r = this.rewardMatrix[x][y];
            return r;
        } else {
            throw new RuntimeException("GridWorld reward matrix is only defined for a " + this.width + "x" + this.height + " world, but the agent transitioned to position (" + x + "," + y + "), which is outside the bounds.");
        }
    }
}
