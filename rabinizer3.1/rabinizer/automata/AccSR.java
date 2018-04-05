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
public class AccSR extends ArrayList<Set<ProductDegenAccState>> {

    AccSR(AccTR accTR, DSRA dsra) {
        super();
        for (int i = 0; i < 2 * accTR.size(); i++) {
            this.add(new HashSet());
        }
        for (ProductDegenAccState s : dsra.states) {
            for (Integer i : s.right) {
                this.get(i).add(s);
            }
        }
    }

    @Override
    public String toString() {
        String result = "Rabin state-based acceptance condition";
        for (int i = 0; i < size() / 2; i++) {
            result += "\nPair " + (i + 1) + "\nFin:\n" + get(2 * i) + "\nInf:\n" + get(2 * i + 1);
        }
        return result;
    }

    String accSets(ProductDegenAccState s) {
        String result = "{";
        Set<Integer> accSets = s.right;
        for (int i = 0; i < 2*size(); i++) {
            if (accSets.contains(i)) {
                result += i + " ";
            }
        }
        return result + "}";
    }

}
