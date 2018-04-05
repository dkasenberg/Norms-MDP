/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.Map;
import java.util.Set;
import rabinizer.exec.Tuple;

/**
 *
 * @author jkretinsky
 */
class ProductAccState extends Tuple<ProductState, Map<Integer, Set<Integer>>> {

    public ProductAccState(ProductState ps, Map<Integer, Set<Integer>> accSets) {
        super(ps, accSets);
    }
    
    @Override
    public String toString(){
        return left + " " + right;
    }

}
