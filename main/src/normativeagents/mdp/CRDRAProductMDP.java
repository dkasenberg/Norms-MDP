/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.mdp;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import normativeagents.NonOOPropFunction;
import normativeagents.actions.CRDRAActionType;
import normativeagents.mdp.model.CRDRAProductModel;
import normativeagents.mdp.state.CRDRAProductState;
import normativeagents.parsing.LTLNorm;
import normativeagents.rabin.CRDRA;
import normativeagents.rabin.RabinAutomaton;
import rabinizer.bdd.Globals;
import rabinizer.bdd.Valuation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static normativeagents.mdp.state.RecordedActionState.LAST_ACTION;

/**
 *
 * @author dkasenberg
 */
public class CRDRAProductMDP extends WrappedMDPContainer {
    public List<CRDRA> crdras;
    public List<LTLNorm> norms;
    public static final String RABINCLASS = "rabin";
    public static final String ATTSTATE = "rabinState";
    public Globals globals;
    
    public CRDRAProductMDP(MDPContainer mdp, List<LTLNorm> norms, List<CRDRA> crdras, Globals globals) {
        super(mdp);
        this.crdras = crdras;
        this.norms = norms;
//        this.wnras = norms.stream().flatMap(n -> n.normInstances.stream()).map(ni -> ni.crdra).collect(Collectors.toList());

        SADomain d = mdp.domain;

        SADomain newDomain;

        if(d instanceof OOSADomain) {
            newDomain = new OOSADomain();
            OOSADomain orig = (OOSADomain)d;
            for(PropositionalFunction pf : orig.propFunctions()) {
                ((OOSADomain)newDomain).addPropFunction(pf);
            }
            // TODO add state classes somehow?
        } else {
            throw new RuntimeException("Need an OOMDP for product functionality at this point");
        }

        d.getActionTypes().stream().forEach(aT -> {
            newDomain.addActionType(new CRDRAActionType(aT, this, norms,crdras));
        });


        FactoredModel fm = (FactoredModel)d.getModel();

        newDomain.setModel(new FactoredModel(new CRDRAProductModel(fm.getStateModel(), this), fm.getRf(), new CRDRAProductTF(fm.getTf())));
        SeeFirstStateActionType a = new SeeFirstStateActionType(initialState);
        newDomain.addActionType(new CRDRAActionType(a, this, norms, crdras));
        this.domain = newDomain;

        this.initialState = new CRDRAProductState(NullState.instance, Collections.nCopies(crdras.size(), 0));
    }
    
    public Valuation getValuation(State s, CRDRA wnra) {
        List<Boolean> allVarValues = new ArrayList<>();
        wnra.formulaVars.stream().forEach((var) -> {
            allVarValues.add(singlePropFunValuation(var, s));
        });
        return new Valuation(allVarValues, globals);
    }
    
    public boolean singlePropFunValuation(RabinAutomaton.FormulaVar var, State s) {
        if(domain instanceof OOSADomain && ((OOSADomain)domain).propFunction(var.name) != null) {
            try {
                if(!(s instanceof OOState)) {
                    return ((NonOOPropFunction)((OOSADomain)domain).propFunction(var.name)).isTrue(s);
                }
                return ((OOSADomain)domain).propFunction(var.name).isTrue((OOState)s, var.params);
            } catch(Exception e) {
                return false;
            }
        }
        try {

        } catch(Exception e) {

        }
        Action lastAction = (Action)s.get(LAST_ACTION);
//        TODO Will probably need to correct this based on new toString system
        return lastAction.toString() != null && lastAction.toString().equals((var.name + " " + String.join(" ", var.params)).trim());
    }
    
    public int getNextRabinState(int rabinState, CRDRA wnra, State newState) {
        return wnra.succ(rabinState, getValuation(newState, wnra));
    }

    public static class SeeFirstStateActionType extends UniversalActionType {
        public State initialState;
        public static String ACTION_SEE_FIRST_STATE = "SEE_FIRST_STATE";
        
        public SeeFirstStateActionType(State initialState) {
            super(ACTION_SEE_FIRST_STATE);
            this.initialState = initialState;
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            if(s instanceof NullState) return allActions;
            return new ArrayList<>();
        }
        
    }

    public static class CRDRAProductTF implements TerminalFunction {
        protected TerminalFunction innerTF;
        public CRDRAProductTF(TerminalFunction tf) {
            this.innerTF = tf;
        }

        public boolean terminal(State state) {
            if(((CRDRAProductState)state).s instanceof NullState) {
                return false;
            }
            return innerTF.isTerminal(((CRDRAProductState)state).s);
        }


        @Override
        public boolean isTerminal(State state) {
            boolean t = terminal(state);
            return t;
        }
    }
}
