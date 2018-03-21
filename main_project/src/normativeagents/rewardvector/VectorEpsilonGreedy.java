package normativeagents.rewardvector;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


/**
 * This class defines a an epsilon-greedy policy over Q-values and requires a QComputable valueFunction to be specified.
 * With probability epsilon the policy will return a random action (with uniform distribution over all possible action).
 * With probability 1 - epsilon the policy will return the greedy action. If multiple actions tie for the highest Q-value,
 * then one of the tied actions is randomly selected.
 * @author James MacGlashan
 *
 */
public class VectorEpsilonGreedy implements SolverDerivedPolicy, EnumerablePolicy {

    protected QVectorProvider qplanner;
    protected double					epsilon;
    protected Random rand;


    /**
     * Initializes with the value of epsilon, where epsilon is the probability of taking a random action.
     * @param epsilon the probability of taking a random action.
     */
    public VectorEpsilonGreedy(double epsilon) {
        qplanner = null;
        this.epsilon = epsilon;
        rand = RandomFactory.getMapped(0);
    }

    /**
     * Initializes with the QComputablePlanner to use and the value of epsilon to use, where epsilon is the probability of taking a random action.
     * @param planner the QComputablePlanner to use
     * @param epsilon the probability of taking a random action.
     */
    public VectorEpsilonGreedy(QVectorProvider planner, double epsilon) {
        qplanner = planner;
        this.epsilon = epsilon;
        rand = RandomFactory.getMapped(0);
    }


    /**
     * Returns the epsilon value, where epsilon is the probability of taking a random action.
     * @return the epsilon value
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Sets the epsilon value, where epsilon is the probability of taking a random action.
     * @param epsilon the probability of taking a random action.
     */
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    @Override
    public void setSolver(MDPSolverInterface solver){

        if(!(solver instanceof QVectorProvider)){
            throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
        }

        this.qplanner = (QVectorProvider) solver;
    }

    @Override
    public Action action(State s) {


        List<QValueVector> qValues = this.qplanner.qValues(s);


        double roll = rand.nextDouble();
        if(roll <= epsilon){
            int selected = rand.nextInt(qValues.size());
            Action ga = qValues.get(selected).a;
            return ga;
        }
        Comparator comp = qplanner.getComparator();

        List <QValueVector> maxActions = new ArrayList<QValueVector>();
        maxActions.add(qValues.get(0));
        RealVector maxQ = qValues.get(0).q;
        for(int i = 1; i < qValues.size(); i++){
            QValueVector q = qValues.get(i);
            if(comp.compare(q.q, maxQ) == 0){
                maxActions.add(q);
            }
            else if(comp.compare(q.q, maxQ) > 0){
                maxActions.clear();
                maxActions.add(q);
                maxQ = q.q;
            }
        }
        int selected = rand.nextInt(maxActions.size());
        //return translated action parameters if the action is parameterized with objects in a object identifier indepdent domain
        Action ga =  maxActions.get(selected).a;
        return ga;
    }



    @Override
    public double actionProb(State s, Action a) {
        return PolicyUtils.actionProbFromEnum(this, s, a);
    }

    @Override
    public List<ActionProb> policyDistribution(State s) {

        List<QValueVector> qValues = this.qplanner.qValues(s);
        Comparator comp = qplanner.getComparator();
        List <ActionProb> dist = new ArrayList<ActionProb>(qValues.size());
        RealVector maxQ = new ArrayRealVector(qplanner.size(), Double.NEGATIVE_INFINITY);
        int nMax = 0;
        for(QValueVector q : qValues){
            if(comp.compare(q.q, maxQ) > 0){
                maxQ = q.q;
                nMax = 1;
            }
            else if(q.q == maxQ){
                nMax++;
            }
            ActionProb ap = new ActionProb(q.a, this.epsilon*(1. / qValues.size()));
            dist.add(ap);
        }
        for(int i = 0; i < dist.size(); i++){
            QValueVector q = qValues.get(i);
            if(comp.compare(q.q, maxQ) == 0){
                dist.get(i).pSelection += (1. - this.epsilon) / nMax;
            }
        }


        return dist;
    }


    @Override
    public boolean definedFor(State s) {
        return true; //can always find q-values with default value
    }

}
