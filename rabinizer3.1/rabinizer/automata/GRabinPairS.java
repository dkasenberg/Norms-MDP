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
public class GRabinPairS extends GRabinPair<Set<ProductAccState>> {

    public GRabinPairS(Set<ProductAccState> l, List<Set<ProductAccState>> r) {
        super(l, r);
    }
    
}
