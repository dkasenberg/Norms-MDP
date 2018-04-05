/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.Arrays;
import java.util.Set;
import rabinizer.exec.Tuple;

/**
 *
 * @author jkretinsky
 */
public class ProductDegenAccState extends Tuple<ProductDegenState, Set<Integer>> {

    public ProductDegenAccState(ProductDegenState pds, Set<Integer> accSets) {
        super(pds, accSets);
    }

    @Override
    public String toString() {
        String result = left.toString();
        int[] orderedSets = new int[right.size()];
        int i = 0;
        for (Integer set : right) {
            orderedSets[i] = set;
            i++;
        }
        Arrays.sort(orderedSets);
        for (i = 0; i < orderedSets.length; i++) {
            int j = orderedSets[i];
            result += " " + (j % 2 == 1 ? "+" : "-") + (j / 2 + 1);
        }
        return result;
    }

}
