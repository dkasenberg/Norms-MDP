/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.HashMap;
import java.util.Map;
import rabinizer.automata.ProductState;
import rabinizer.bdd.ValuationSet;
import rabinizer.bdd.ValuationSetBDD;

/**
 *
 * @author jkretinsky
 */
public class TranSet<State> extends HashMap<State, ValuationSet> {

    public TranSet<State> add(State s, ValuationSet vs) {
        if (!this.containsKey(s)) {
            this.put(s, vs);
        } else {
            ValuationSet old = new ValuationSetBDD(this.get(s));
            old.add(vs);
            this.put(s, old);
        }
        return this;
    }

    public TranSet<State> addAll(TranSet<State> ts) {
        for (State s : ts.keySet()) {
            this.add(s, ts.get(s));
        }
        return this;
    }

    boolean subsetOf(TranSet<State> ts) {
        for (State s : this.keySet()) {
            if (!ts.containsKey(s) || !ts.get(s).contains(this.get(s))) {
                return false;
            }
        }
        return true;
    }

    void removeAll(TranSet<State> ts) {
        for (State s : ts.keySet()) {
            if (this.containsKey(s)) {
                ValuationSet old = new ValuationSetBDD(this.get(s));
                old.remove(ts.get(s));
                this.put(s, old);
                if (this.get(s).isEmpty()) {
                    this.remove(s);
                }
            }
        }
    }
    
    /*
    @Override
    public int hashCode() {
        return 17 * super.hashCode() + 5 * masterState.hashCode();
    }


    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
        */

    
    public String toString() {
        String result = "{";
        boolean first = true;
        for (State s : this.keySet()) {
            result += (first ? "" : ";\n") + s + "  ->  " + get(s);
            first = false;
        }
        return result + "}";
    }

}
