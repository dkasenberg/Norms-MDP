/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.single.domains.shocks;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author dkasenberg
 * ShockDomain is a domain built to vaguely resemble the trolley problem, where
 * there are various patients who may receive electric shocks, and all you can
 * do is (potentially) flip a switch to change who gets the shock and who \
 * doesn't.
 */
public class ShockDomain implements DomainGenerator {
    public static final String CLASS_VICTIM = "victim";
    public static final String VAR_MAIN = "mainVictim";
    public static final String VAR_ALT = "alternateVictim";
    public static final String VAR_PAIN = "inPain";
    public static final String ACTION_FLIP = "flipSwitch";
    public static final String ACTION_STAY = "doNothing";
    public static final String PF_MAIN = "mainVictim";
    public static final String PF_ALT = "alternateVictim";
    public static final String PF_PAIN = "inPain";
    
    protected Random rand;
    
    
    public ShockDomain() {
        rand = new Random();
    }
    
    public OOSADomain generateDomain(double probSwitchFailure, double probSpontTransition) {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_VICTIM, ShockWorldVictim.class);

        domain.addActionTypes(new FlipActionType(ACTION_FLIP),
                new DoNothingActionType(ACTION_STAY));

        domain.addPropFunction(new MainPF(PF_MAIN))
                .addPropFunction(new AltPF(PF_ALT))
                .addPropFunction(new PainPF(PF_PAIN));

        ShockWorldModel smodel = new ShockWorldModel(probSwitchFailure, probSpontTransition);
        FactoredModel model = new FactoredModel(smodel, new NullRewardFunction(), new NullTermination());
        domain.setModel(model);
        
        return domain;
    }
    
    @Override
    public OOSADomain generateDomain() {
        return generateDomain(0.0, 0.0);
    }
    
    public static ShockWorldState oneAgentNoVictims(OOSADomain d) {
        return new ShockWorldState();
    }
    
    public static ShockWorldState addVictim(OOSADomain d, State s) {
        ShockWorldState st = (ShockWorldState)s;
        st.addObject(new ShockWorldVictim(CLASS_VICTIM + st.numObjects()));
        return st;
    }

    public static class MainPF extends PropositionalFunction {

        public MainPF(String name) {
            super(name, new String[]{CLASS_VICTIM});
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((ShockWorldVictim)s.object(params[0])).main;
        }
    }

    public static class PainPF extends PropositionalFunction {

        public PainPF(String name) {
            super(name, new String[]{CLASS_VICTIM});
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((ShockWorldVictim)s.object(params[0])).inPain;
        }
    }

    public static class AltPF extends PropositionalFunction{

        public AltPF(String name) {
            super(name, new String[]{CLASS_VICTIM});
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((ShockWorldVictim)s.object(params[0])).alternate;
        }
    }

    private class FlipActionType extends UniversalActionType {

        public FlipActionType(String typeName) {
            super(typeName);
        }

        @Override
        public List<Action> allApplicableActions(State s) {
            if(((OOState)s).objectsOfClass(CLASS_VICTIM).stream()
                    .allMatch(v -> ((ShockWorldVictim)v).alternate || ((ShockWorldVictim)v).main)) {
                return this.allActions;
            }
            return new ArrayList<>();
        }
    }

    private class DoNothingActionType extends UniversalActionType {

        public DoNothingActionType(String name) {
            super(name);
        }
    }
    
}
