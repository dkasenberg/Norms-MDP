package normativeagents.rewardvector;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * A {@link VectorValueFunction} implementation that always returns a constant value.
 * @author James MacGlashan
 *
 */
public class ConstantVectorValueFunction implements QVectorFunction {

    /**
     * The constant value to return for all initializations.
     */
    public double value = 0;
    public int size = 0;

    /**
     * Will cause this object to return 0 for all initialization values.
     */
    public ConstantVectorValueFunction(int size){
        //defaults value to zero
        this.size = size;
    }


    /**
     * Will cause this object to return <code>value</code> for all initialization values.
     * @param value the value to return for all initializations.
     */
    public ConstantVectorValueFunction(double value, int size){
        this.value = value;
        this.size = size;
    }

    @Override
    public RealVector value(State s) {
        return new ArrayRealVector(size, value);
    }

    public RealVector qValue(State s, Action a) {
        return new ArrayRealVector(size, value);
    }

    @Override
    public int size() {
        return size;
    }


}