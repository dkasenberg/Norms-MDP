/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rabinizer.automata;

import java.util.*;
import rabinizer.exec.Tuple;

/**
 *
 * @author jkretinsky
 */
public class GRabinPair<BaseSet> extends Tuple<BaseSet, List<BaseSet>> {

    public GRabinPair(BaseSet l, List<BaseSet> r) {
        super(l, r);
    }
    
    /*
    public GRabinPair(BaseSet l, Set<BaseSet> r) {
        //super(l, order(r));
        super(l, null);
        right = order(r);        
    }
    */
    
    public final List<BaseSet> order(Set<BaseSet> r) {
        List<BaseSet> result = new ArrayList(r.size());
        for (BaseSet ts : r) {
            result.add(ts);
        }
        return result;
    }

    public String toString() {
        String result = "Fin:\n" + (left == null ? "trivial" : left) + "\nInf: ";
        if (right == null || right.isEmpty()) {
            result += "0\ntrivial";
        } else {
            result += right.size();
            for (BaseSet inf : right) {
                result += "\n" + inf;
            }
        }
        return result;
    }
    
    
}
