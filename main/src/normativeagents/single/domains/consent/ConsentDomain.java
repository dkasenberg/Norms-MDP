package normativeagents.single.domains.consent;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.NullRewardFunction;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import normativeagents.NonOOPropFunction;

import static normativeagents.single.domains.consent.ConsentState.*;

/**
 * Created by dkasenberg on 10/24/17.
 */
public class ConsentDomain implements DomainGenerator {

    public static final String ACTION_CARE = "care";
    public static final String ACTION_NOCARE = "nocare";
    public static final String PF_SAIDCONSENT = "consentGiven";
    public static final String PF_SAIDNOCONSENT = "consentWithdrawn";
    public static final String PF_CAREGIVEN = "careGiven";
    public double signalProbability = 0.05;

    public ConsentDomain(double signalProbability) {
        this.signalProbability = signalProbability;
    }


    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();

        domain.addPropFunction(new BooleanAttributePF(PF_CAREGIVEN, ATT_CAREGIVEN))
                .addPropFunction(new BooleanAttributePF(PF_SAIDCONSENT, ATT_SAIDCONSENT))
                .addPropFunction(new BooleanAttributePF(PF_SAIDNOCONSENT, ATT_SAIDNOCONSENT))
                .addActionType(new UniversalActionType(ACTION_CARE))
                .addActionType(new UniversalActionType(ACTION_NOCARE));

        ConsentModel model = new ConsentModel(this.signalProbability);
        domain.setModel(new FactoredModel(model,new NullRewardFunction(), new NullTermination()));

        return domain;
    }

    public static class BooleanAttributePF extends NonOOPropFunction {

        public String attributeName;

        public BooleanAttributePF(String name, String attributeName) {
            super(name);
            this.attributeName = attributeName;
        }

        @Override
        public boolean isTrue(State s) {
            return (Boolean)s.get(attributeName);
        }
    }
}
