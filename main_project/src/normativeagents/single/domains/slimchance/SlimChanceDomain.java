/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.single.domains.slimchance;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import normativeagents.NonOOPropFunction;

/**
 *
 * @author dkasenberg
 */
public class SlimChanceDomain implements DomainGenerator {

    public static final String ACTION_TRY = "try";
    public static final String ACTION_NOTRY = "notry";
    public static final String VAR_GOOD = "good";
    public static final String PF_GOOD = "good";
    
    protected double probOfSuccess;
    
    public SlimChanceDomain(double probOfSuccess) {
        this.probOfSuccess = probOfSuccess;
    }
    
    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addPropFunction(new GoodPF(PF_GOOD))
                .addActionType(new UniversalActionType(ACTION_TRY))
                .addActionType(new UniversalActionType(ACTION_NOTRY));

        SlimChanceModel stateModel = new SlimChanceModel(probOfSuccess);
        domain.setModel(new FactoredModel(stateModel, new NullRewardFunction(), new NullTermination()));

        return domain;
    }

    public static class GoodPF extends NonOOPropFunction {
        public GoodPF(String name) {
            super(name);
        }

        @Override
        public boolean isTrue(State s) {
            return (Boolean)s.get(VAR_GOOD);
        }
    }
    
    
}
