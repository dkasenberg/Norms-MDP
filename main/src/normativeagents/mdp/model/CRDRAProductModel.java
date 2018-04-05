package normativeagents.mdp.model;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;
import normativeagents.actions.CRDRAAction;
import normativeagents.mdp.CRDRAProductMDP;
import normativeagents.mdp.state.CRDRAProductState;
import normativeagents.rabin.CRDRA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static normativeagents.actions.CRDRAAction.*;
import static normativeagents.mdp.CRDRAProductMDP.SeeFirstStateActionType.ACTION_SEE_FIRST_STATE;

/**
 * Created by dan on 5/16/17.
 */
public class CRDRAProductModel implements FullStateModel {
    public FullStateModel model;
    public List<CRDRA> crdras;
    public CRDRAProductMDP mdp;

    public CRDRAProductModel(SampleStateModel model, CRDRAProductMDP mdp) {
        if(!(model instanceof FullStateModel)) throw new RuntimeException("Must be a FullStateModel");
        this.model = (FullStateModel)model;
        this.mdp = mdp;
        this.crdras = mdp.crdras;
    }


    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        CRDRAAction pa = (CRDRAAction)a;
        CRDRAProductState ps = (CRDRAProductState)s;

        if(pa.action.actionName().equals(ACTION_SEE_FIRST_STATE)) {
            return Arrays.asList(new StateTransitionProb(getProductTransition(ps, pa, mdp.mdp.initialState), 1.0));
        }

        return model.stateTransitions(ps.s, pa.action)
                .stream()
                .map(tp -> {
                    State sp = getProductTransition(ps, pa, tp.s);
                    return new StateTransitionProb(sp, tp.p);
                }).collect(Collectors.toList());

    }

    public State getProductTransition(CRDRAProductState s, CRDRAAction a, State sp) {
        int settingsCounter = 0;

        List<Integer> newQs = new ArrayList<>();
        for(int i = 0; i < crdras.size(); i++) {
            switch(a.settings[settingsCounter]) {
                case GIVEUP:
                    newQs.add(crdras.get(i).ACC);
                    break;
                case BREAKONCE:
                    newQs.add(s.qs.get(i));
                    break;
                case MAINTAIN:
                    newQs.add(mdp.getNextRabinState(s.qs.get(i), crdras.get(i), sp));
            }
            settingsCounter++;
        }
        return new CRDRAProductState(sp, newQs);
    }

    public State sample(State s, Action a) {
        return null;
    }
}
