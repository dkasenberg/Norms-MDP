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
public class AccSGR extends ArrayList<GRabinPairS> {

    protected Map<Integer, Map<Integer, Integer>> seq = new HashMap();

    AccSGR(AccTGR accTGR, DSGRA dsgra) {
        super();
        int sum = 0;
        for (int i = 0; i < accTGR.size(); i++) {
            this.add(new GRabinPairS(new HashSet(), new ArrayList(accTGR.get(i).right.size())));
            seq.put(i, new HashMap());
            seq.get(i).put(-1, sum);
            sum++;
            for (int j = 0; j < accTGR.get(i).right.size(); j++) {
                this.get(i).right.add(new HashSet());
                seq.get(i).put(j, sum);
                sum++;
            }
        }
        for (ProductAccState s : dsgra.states) {
            for (Integer i : s.right.keySet()) {
                for (Integer j : s.right.get(i)) {
                    if (j == -1) {
                        this.get(i).left.add(s);
                    } else //System.out.println(this.get(i).right.size() + " AccSG " + j);
                    {
                        this.get(i).right.get(j).add(s);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        String result = "Gen. Rabin state-based acceptance condition";
        for (int i = 0; i < size(); i++) {
            result += "\nPair " + (i + 1) + "\nFin:\n" + get(i).left + "\nInf:\n";
            for (int j = 0; j < get(i).right.size(); j++) {
                result += get(i).right.get(j) + "\n";
            }
        }
        return result;
    }

    String accSets(ProductAccState s) {
        String result = "";
        for (Integer i : s.right.keySet()) {
            for (Integer j : s.right.get(i)) {
                result += seq.get(i).get(j) + " ";
            }
        }
        return result;
    }

}
