/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.*;
/**
 *
 * @author jkretinsky
 */
public class GRabinPairT extends GRabinPair<TranSet<ProductState>> {

    public GRabinPairT(TranSet<ProductState> l, List<TranSet<ProductState>> r) {
        super(l, r);
    }
    
    /*
    public List<TranSet<ProductState>> order() {
        List<TranSet<ProductState>> result = new ArrayList(right.size());
        for (TranSet<ProductState> ts : right) {
            result.add(ts);
        }
        return result;
    }
    
    
    public GRabinPairT(GRabinPairT pair) {
        super(pair.left, pair.right);
    }
    */

    
}
