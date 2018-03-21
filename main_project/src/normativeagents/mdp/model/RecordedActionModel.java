package normativeagents.mdp.model;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;
import normativeagents.mdp.state.RecordedActionState;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by dan on 5/16/17.
 */
public class RecordedActionModel implements FullStateModel {

    public SampleStateModel model;

    public RecordedActionModel(SampleStateModel model) {
        this.model = model;
    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        RecordedActionState st = (RecordedActionState)s;
        if(!(model instanceof FullStateModel)) {
            return FullStateModel.Helper.deterministicTransition(this, st.s, a);
        }
        FullStateModel fsm = (FullStateModel)model;
        return fsm.stateTransitions(st.s, a)
                .stream()
                .map(trans -> new StateTransitionProb(new RecordedActionState(trans.s, a), trans.p))
                .collect(Collectors.toList());
    }

    @Override
    public State sample(State s, Action a) {
        RecordedActionState st = (RecordedActionState)s;
        return new RecordedActionState(model.sample(st.s, a), a);
    }
}
