/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.math3.linear.RealVector;

import java.util.Map;

/**
 *
 * @author dkasenberg
 */
public class FromMapVectorVF implements QVectorFunction {

    public Map<HashableState, RealVector> initialValues;
    public HashableStateFactory hashingFactory;
    public RealVector defaultValue;
    
    public FromMapVectorVF(Map<HashableState, RealVector> initialValues, HashableStateFactory hf, RealVector defaultValue) {
        this.initialValues = initialValues;
        this.hashingFactory = hf;
        this.defaultValue = defaultValue;
    }

    @Override
    public RealVector qValue(State s, Action a) {
        return value(s);
    }

    @Override
    public int size() {
        return defaultValue.getDimension();
    }

    @Override
    public RealVector value(State s) {
        return value(hashingFactory.hashState(s));
    }
    
    public RealVector value(HashableState s) {
        if(initialValues.containsKey(s)) return initialValues.get(s).copy();
        return defaultValue.copy();
    }
    
}
