package normativeagents.statehashing;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.statehashing.simple.IDSimpleHashableState;
import normativeagents.mdp.state.WrapperState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Created by dan on 5/18/17.
 */
public class HashableWrapperState extends IDSimpleHashableState {

    public HashableWrapperState() {
        super();
    }

    public HashableWrapperState(State s) {
        super(s);
    }

    @Override
    public int hashCode() {
//        return computeHashCode(this.s);
        return computeWrappedHashCode(this.s);
    }

    protected int computeWrappedHashCode(State s) {
        if(s instanceof WrapperState) {
            HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
            WrapperState wrapperState = (WrapperState)s;
            State wrappedState = wrapperState.s;
            int wrappedStateHash = this.computeWrappedHashCode(wrappedState);
            this.appendHashCodeForValue(hashCodeBuilder, "wrappedState", wrappedStateHash);
            List<Object> keys = wrapperState.uniqueKeys();
            for(Object key : keys){
                Object value = s.get(key);
                if(value instanceof Action) {
                    this.appendHashCodeForValue(hashCodeBuilder, key, value.toString());
                    continue;
                }
                this.appendHashCodeForValue(hashCodeBuilder, key, value);
            }
            return hashCodeBuilder.toHashCode();
        }
        return computeHashCode(s);
    }

    @Override
    protected boolean statesEqual(State s1, State s2) {
        if(s1 instanceof WrapperState && s2 instanceof WrapperState) {
            WrapperState s1w = (WrapperState)s1;
            WrapperState s2w = (WrapperState)s2;

            return uniqueKeysEqual(s1w, s2w) && statesEqual(s1w.s, s2w.s);
        }
        return super.statesEqual(s1,s2);

    }

    protected boolean uniqueKeysEqual(WrapperState s1, WrapperState s2) {
        if(s1 == s2){
            return true;
        }

        List<Object> keys1 = s1.uniqueKeys();
        List<Object> keys2 = s2.uniqueKeys();

        if(keys1.size() != keys2.size()){
            return false;
        }

        for(Object key : keys1){
            Object v1 = s1.get(key);
            Object v2 = s2.get(key);
            if(!this.valuesEqual(key, v1, v2)){
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether two values are equal.
     * @param key the state variable key
     * @param v1 the first value to compare
     * @param v2 the second value to compare
     * @return true if v1 = v2; false otherwise
     */
    @Override
    protected boolean valuesEqual(Object key, Object v1, Object v2){
        if(v1 == null && v2 == null) return true;
        else if(v1 == null || v2 == null) return false;
        return super.valuesEqual(key, v1, v2);
    }


}
