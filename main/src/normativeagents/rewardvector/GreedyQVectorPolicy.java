package normativeagents.rewardvector;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.RealVector;

import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * A greedy policy that breaks ties by randomly choosing an action amongst the tied actions. This class requires a QComputablePlanner
 * @author James MacGlashan
 *
 */
public class GreedyQVectorPolicy implements SolverDerivedPolicy, EnumerablePolicy {

    protected QVectorProvider qplanner;
    protected Random rand;


    public GreedyQVectorPolicy(){
        qplanner = null;
        rand = RandomFactory.getMapped(0);
    }


    /**
     * Initializes with a QComputablePlanner
     * @param planner the QComputablePlanner to use
     */
    public GreedyQVectorPolicy(QVectorProvider planner){
        qplanner = planner;
        rand = RandomFactory.getMapped(0);
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
        List <QValueVector> maxActions = new ArrayList<QValueVector>();
        maxActions.add(qValues.get(0));
        RealVector maxQ = qValues.get(0).q;
        for(int i = 1; i < qValues.size(); i++){
            QValueVector q = qValues.get(i);
            if(qplanner.getComparator().compare(q.q, maxQ) == 0){
                maxActions.add(q);
            }
            else if(qplanner.getComparator().compare(q.q, maxQ) > 0){
                maxActions.clear();
                maxActions.add(q);
                maxQ = q.q;
            }
        }
        int selected = rand.nextInt(maxActions.size());
        //return translated action parameters if the action is parameterized with objects in a object identifier independent domain
        Action srcA = maxActions.get(selected).a;
        return srcA;
    }

    @Override
    public double actionProb(State s, Action a) {
        return PolicyUtils.actionProbFromEnum(this, s, a);
    }

    public List<ActionProb> policyDistribution(State s) {
        List<QValueVector> qValues = this.qplanner.qValues(s);
        int numMax = 1;
        RealVector maxQ = qValues.get(0).q;
        for(int i = 1; i < qValues.size(); i++){
            QValueVector q = qValues.get(i);
            if(qplanner.getComparator().compare(q.q, maxQ) == 0){
                numMax++;
            }
            else if(qplanner.getComparator().compare(q.q, maxQ) > 0){
                numMax = 1;
                maxQ = q.q;
            }
        }

        List <ActionProb> res = new ArrayList<ActionProb>();
        double uniformMax = 1./(double)numMax;
        for(int i = 0; i < qValues.size(); i++){
            QValueVector q = qValues.get(i);
            double p = 0.;
            if(qplanner.getComparator().compare(q.q, maxQ) == 0){
                p = uniformMax;
            }
            ActionProb ap = new ActionProb(q.a, p);
            res.add(ap);
        }


        return res;
    }



    @Override
    public boolean definedFor(State s) {
        return true; //can always find q-values with default value
    }







}
