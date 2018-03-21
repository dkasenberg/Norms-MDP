package normativeagents.mdp.model;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.mdp.singleagent.model.statemodel.SampleStateModel;
import normativeagents.actions.CRDRAAction;
import normativeagents.mdp.CRDRAProductMDP;
import normativeagents.mdp.state.CRDRAProductState;
import normativeagents.parsing.LTLNorm;
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
            return Arrays.asList(new StateTransitionProb(getRabinizedState(ps, pa, mdp.mdp.initialState), 1.0));
        }

        return model.stateTransitions(ps.s, pa.action)
                .stream()
                .map(tp -> {
                    State sp = getRabinizedState(ps, pa, tp.s);
                    return new StateTransitionProb(sp, tp.p);
                }).collect(Collectors.toList());

//                .getTransitions(derabinizeState(s), groundedAction)
//                .stream()
//                .map(tp -> {
//                    if(!(groundedAction instanceof CRDRAAction)) return tp;
//                    CRDRAAction ga = (CRDRAAction)groundedAction;
//                    State sp = getRabinizedState(s, ga, tp.s);
//                    return new TransitionProbability(sp, tp.p);
//                }).collect(Collectors.toList());
    }

    public State getRabinizedState(CRDRAProductState s, CRDRAAction a, State derabinizedSp) {
        int settingsCounter = 0;
        List<Integer> newQs = new ArrayList<>();
        for(int i = 0; i < crdras.size(); i++) {
            LTLNorm norm = crdras.get(i).norm;
            if(norm.isNumerical) {
                for(int j = i; j < i + norm.normInstances.size(); j++) {
                    if(s.qs.get(j) == -1 && a.settings[settingsCounter] == GIVEUP) {
                        newQs.add(crdras.get(j).ACC);
                    } else {
                        int newState = mdp.getNextRabinState(s.qs.get(j), crdras.get(j), derabinizedSp);
                        if(a.settings[settingsCounter + newState + 1] == MAINTAIN) {
                            newQs.add(newState);
                        }
                    }
                }
                i += norm.normInstances.size() - 1;
                settingsCounter += norm.wnraSize;
            } else {
                switch(a.settings[settingsCounter]) {
                    case GIVEUP:
                        newQs.add(crdras.get(i).ACC);
                        break;
                    case BREAKONCE:
                        newQs.add(s.qs.get(i));
                        break;
                    case MAINTAIN:
                        newQs.add(mdp.getNextRabinState(s.qs.get(i), crdras.get(i), derabinizedSp));
                }
                settingsCounter++;
            }
        }
        return new CRDRAProductState(derabinizedSp, newQs);
    }

    public State sample(State s, Action a) {
        return null;
    }
}
