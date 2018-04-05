/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.*;
import rabinizer.bdd.Valuation;
import rabinizer.bdd.ValuationSet;
import rabinizer.bdd.ValuationSetBDD;
import rabinizer.exec.Tuple;
import rabinizer.formulas.Formula;

/**
 *
 * @author jkretinsky
 */
public class AccTR extends ArrayList<RabinPair<ProductDegenState>> {

    public AccTR(AccTGR accTGR, DTRA dtra) {
        super();
        for (int i = 0; i < accTGR.size(); i++) {
            GRabinPairT grp = accTGR.get(i);
            TranSet<ProductDegenState> fin = new TranSet();
            TranSet<ProductDegenState> inf = new TranSet();
            for (ProductDegenState s : dtra.states) {
                ValuationSet vsFin = grp.left.get(s.left);
                if (vsFin != null) {
                    fin.add(s, vsFin);
                }
                if (s.right.get(i) == grp.right.size()) {
                    inf.add(s, dtra.globals.vsBDD.getAllVals());
                }
            }
            this.add(new RabinPair(fin, inf));
        }
    }

    @Override
    public String toString() {
        String result = "Rabin transition-based acceptance condition";
        int i = 1;
        for (RabinPair pair : this) {
            result += "\nPair " + i + "\n" + pair.toString();
            i++;
        }
        return result;
    }

    String accSets(ProductDegenState s, Valuation v) {
        String result = "";
        for (int i = 0; i < size(); i++) {
            if (get(i).left.containsKey(s) && get(i).left.get(s).contains(v)) {
                result += 2 * i + " ";
            }
            if (get(i).right.containsKey(s) && get(i).right.get(s).contains(v)) {
                result += (2 * i + 1) + " ";
            }
        }
        return result;
    }

}
