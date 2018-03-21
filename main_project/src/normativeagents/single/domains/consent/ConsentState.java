package normativeagents.single.domains.consent;

import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dkasenberg on 10/24/17.
 */
public class ConsentState implements MutableState {

    public boolean careGiven;
    public boolean saidConsent;
    public boolean saidNoConsent;

    public static final String ATT_CAREGIVEN = "careGiven";
    public static final String ATT_SAIDCONSENT = "saidConsent";
    public static final String ATT_SAIDNOCONSENT = "saidNoConsent";

    public static final List<Object> keys = Arrays.asList(ATT_CAREGIVEN,ATT_SAIDCONSENT, ATT_SAIDNOCONSENT);

    public ConsentState(boolean careGiven, boolean saidConsent, boolean saidNoConsent) {
        this.careGiven = careGiven;
        this.saidConsent = saidConsent;
        this.saidNoConsent = saidNoConsent;
    }

    @Override
    public MutableState set(Object o, Object o1) {
        boolean boolVal = StateUtilities.stringOrBoolean(o1);
        if(o.equals(ATT_CAREGIVEN)) {
            this.careGiven = boolVal;
        } else if(o.equals(ATT_SAIDNOCONSENT)) {
            this.saidNoConsent = boolVal;
        } else if(o.equals(ATT_SAIDCONSENT)) {
            this.saidConsent = boolVal;
        }
        return this;
    }

    @Override
    public List<Object> variableKeys() {
        return new ArrayList<>(keys);
    }

    @Override
    public Object get(Object o) {
        if(o.equals(ATT_CAREGIVEN)) return this.careGiven;
        else if(o.equals(ATT_SAIDCONSENT)) return this.saidConsent;
        else if(o.equals(ATT_SAIDNOCONSENT)) return this.saidNoConsent;
        throw new RuntimeException("Key " + o + " not found.");
    }

    @Override
    public String toString() {
        return "(" + (careGiven ? "CARE" : "NOCARE") + (saidConsent ? "/C" : saidNoConsent ? "/N" : "") + ")";
    }

    @Override
    public State copy() {
        return new ConsentState(this.careGiven,this.saidConsent,this.saidNoConsent);
    }
}
