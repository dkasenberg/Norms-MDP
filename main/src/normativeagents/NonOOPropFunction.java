package normativeagents;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;

/**
 * Created by dkasenberg on 7/28/17.
 */
public abstract class NonOOPropFunction extends PropositionalFunction {

    public NonOOPropFunction(String name) {
        super(name, new String[]{});
    }

    public abstract boolean isTrue(State s);

    @Override
    public boolean isTrue(OOState ooState, String... strings) {
        return isTrue(ooState);
    }

    @Override
    public boolean someGroundingIsTrue(OOState s) {
        return isTrue(s);
    }


}
