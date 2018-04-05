package normativeagents.single.domains;

import burlap.mdp.core.oo.state.ObjectInstance;

/**
 * Created by dkasenberg on 8/3/17.
 */
public interface MutableObjectInstance extends ObjectInstance {

    void set(Object key, Object value);
}
