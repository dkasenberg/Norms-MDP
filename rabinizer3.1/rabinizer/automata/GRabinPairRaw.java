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
public class GRabinPairRaw extends Tuple<TranSet<ProductState>, Set<TranSet<ProductState>>> {

    public GRabinPairRaw(TranSet<ProductState> l, Set<TranSet<ProductState>> r) {
        super(l, r);
    }

    public String toString() {
        String result = "Fin:\n" + (left == null ? "trivial" : left) + "\nInf: ";
        if (right == null || right.isEmpty()) {
            result += "0\ntrivial";
        } else {
            result += right.size();
            for (TranSet<ProductState> inf : right) {
                result += "\n" + inf;
            }
        }
        return result;
    }
    
    public GRabinPairT order() {
        List<TranSet<ProductState>> rightOrdered = new ArrayList(right.size());
        for (TranSet<ProductState> ts : right) {
            rightOrdered.add(ts);
        }
        return new GRabinPairT(left, rightOrdered);
    }
        
}
