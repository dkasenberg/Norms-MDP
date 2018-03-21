package normativeagents.misc;

import java.util.HashMap;

/**
 * Created by dkasenberg on 9/27/17.
 */
public class DefaultHashMap<K,V> extends HashMap<K,V> {

    protected V defaultValue;
    public DefaultHashMap(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public V get(Object key) {
        return containsKey(key) ? super.get(key) : defaultValue;
    }
}
