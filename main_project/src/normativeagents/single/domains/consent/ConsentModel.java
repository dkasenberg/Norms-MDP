package normativeagents.single.domains.consent;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.consent.ConsentDomain.ACTION_CARE;
import static normativeagents.single.domains.consent.ConsentDomain.ACTION_NOCARE;

/**
 * Created by dkasenberg on 10/24/17.
 */
public class ConsentModel implements FullStateModel {

    public double signalProbability;

    public ConsentModel(double signalProbability) {
        this.signalProbability = signalProbability;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State state, Action action) {
        String aname = action.actionName();
        ConsentState newState = new ConsentState(true,false,false);
        if(aname.equals(ACTION_CARE)) {
            newState.careGiven = true;
        } else if(aname.equals(ACTION_NOCARE)) {
            newState.careGiven = false;
        } else {
            throw new RuntimeException("Action " + aname + " not recognized.");
        }
        State consentSignalGiven = new ConsentState(newState.careGiven,true,false);
        State consentWithdrawn = new ConsentState(newState.careGiven,false,true);

        return Arrays.asList(new StateTransitionProb(newState,1.-signalProbability),
                            new StateTransitionProb(consentSignalGiven, signalProbability/2.),
                            new StateTransitionProb(consentWithdrawn, signalProbability/2.));
    }

    @Override
    public State sample(State state, Action action) {
        return Helper.sampleByEnumeration(this,state,action);
    }
}
