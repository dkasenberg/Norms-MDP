/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.HashSet;
import rabinizer.automata.ProductState;
import rabinizer.exec.Tuple;

/**
 *
 * @author jkretinsky
 */
public class RabinAcc extends HashSet<RabinPair> {

    @Override
    public String toString() {
        String result = "\n";
        int i = 1;
        for (Tuple<TranSet<ProductState>, TranSet<ProductState>> pair : this) {
            result += (i==1 ? "" : "\n") + "Pair " + i + "\n" + pair;
        }
        return result;
    }

}
