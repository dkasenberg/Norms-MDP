/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.ArrayList;
import rabinizer.bdd.Valuation;

public class AccTGR extends ArrayList<GRabinPairT> {

    public AccTGR(AccTGRRaw accTGR) {
        super();
        for (GRabinPairRaw grp : accTGR) {
            add(grp.order());
        }
    }

    String accSets(ProductState s, Valuation v) {
        String result = "";
        int sum = 0;
        for (int i = 0; i < size(); i++) {
            if (get(i).left.containsKey(s) && get(i).left.get(s).contains(v)) {
                result += sum + " ";
            }
            sum++;
            for (TranSet<ProductState> ts : get(i).right) {
                if (ts.containsKey(s) && ts.get(s).contains(v)) {
                    result += sum + " ";
                }
                sum++;
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        String result = "Gen. Rabin acceptance condition";
        int i = 1;
        for (GRabinPairT pair : this) {
            result += "\nPair " + i + "\n" + pair.toString();
            i++;
        }
        return result;
    }

}
