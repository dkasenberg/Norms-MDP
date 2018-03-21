/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.single.domains.moralrisk;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.GoalConditionTF;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;

import java.util.Random;

/**
 *
 * @author dkasenberg
 * ShockDomain is a domain built to vaguely resemble the trolley problem, where
 * there are various patients who may receive electric shocks, and all you can
 * do is (potentially) flip a switch to change who gets the shock and who \
 * doesn't.
 */
public class MoralRiskDomain implements DomainGenerator {
    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_VICTIM = "patient";
    
    public static final String CLASS_STATUS = "STATUS";
    public static final String VAR_DONE = "done";
    public static final String PF_DONE = "done";
    
    public static final String VAR_DEAD = "dead";
    public static final String PF_DEAD = "dead";
    
    public static final String ACTION_GAMBLE = "gamble";
    public static final String ACTION_STAY = "stay";
    
    protected Random rand;
    
    
    public MoralRiskDomain() {
        rand = new Random();
    }
    
    public OOSADomain generateDomain(int defaultDead, double probOfSaving) {
        OOSADomain domain = new OOSADomain();

        domain.addStateClass(CLASS_VICTIM, MoralRiskVictim.class)
                .addPropFunction(new DeadPF(PF_DEAD))
                .addActionType(new UniversalActionType(ACTION_GAMBLE))
                .addActionType(new UniversalActionType(ACTION_STAY));

        TerminalFunction tf = new GoalConditionTF(new StateConditionTest() {
            @Override
            public boolean satisfies(State s) {
                return ((MoralRiskState)s).done;
            }
        });

        MoralRiskModel stateModel =new MoralRiskModel(probOfSaving, defaultDead);
        FactoredModel model = new FactoredModel(stateModel, new NullRewardFunction(), tf);
        domain.setModel(model);

        return domain;
    }
    
    @Override
    public Domain generateDomain() {
        return generateDomain(400, 0.5);
    }
    
    public static MoralRiskState oneAgentNVictims(int totalVictims) {
        MoralRiskState s = oneAgentNoVictims();
        for(int i = 0; i < totalVictims; i++) {
            addVictim(s);
        }
        return s;
    }
    
    public static MoralRiskState oneAgentNoVictims() {
        return new MoralRiskState();
    }
    
    public static MoralRiskVictim addVictim(MoralRiskState s) {
        MoralRiskVictim o = new MoralRiskVictim(CLASS_VICTIM + s.victims.size());
        s.addObject(o);
        return o;
    }

    public static class DeadPF extends PropositionalFunction {

        public DeadPF(String name) {
            super(name, new String[]{CLASS_VICTIM});
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            return ((MoralRiskVictim) s.object(params[0])).dead;
        }
    }
    
}
