package normativeagents.single.domains.moralrisk;

import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.moralrisk.MoralRiskDomain.CLASS_VICTIM;
import static normativeagents.single.domains.moralrisk.MoralRiskDomain.VAR_DEAD;

/**
 * Created by dan on 5/17/17.
 */
public class MoralRiskVictim implements ObjectInstance {

    public String name;
    public boolean dead;

    public List<Object> keys = Arrays.asList(VAR_DEAD);

    public MoralRiskVictim(String name, boolean dead) {
        this.name = name;
        this.dead = dead;
    }

    public MoralRiskVictim(String name) {
        this.name = name;
        this.dead = false;
    }

    public MoralRiskVictim() {
        this.name = CLASS_VICTIM;
        this.dead = false;
    }

    @Override
    public String className() {
        return CLASS_VICTIM;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new MoralRiskVictim(objectName, dead);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(VAR_DEAD)) {
            return dead;
        }
        throw new RuntimeException("Unknown key " + variableKey);
    }

    @Override
    public State copy() {
        return new MoralRiskVictim(name, dead);
    }
}
