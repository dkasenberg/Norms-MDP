/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import java.util.*;
import rabinizer.bdd.GSet;
import rabinizer.exec.Main;
import rabinizer.formulas.Formula;

/**
 *
 * @author jkretinsky
 */
public class AccTGRRaw extends HashSet<GRabinPairRaw> {

    private final TranSet<ProductState> allTrans;

    public AccTGRRaw() {
        super();
        allTrans = null;
    }

    public AccTGRRaw(TranSet<ProductState> allTrans) {
        super();
        this.allTrans = allTrans;
    }

    public AccTGRRaw(AccTGRRaw accTGR) {
        super(accTGR);
        allTrans = accTGR.allTrans;
    }

    public AccTGRRaw(AccLocal accLocal) {
        super();
        allTrans = accLocal.allTrans;
        for (GSet gSet : accLocal.accMasterOptions.keySet()) {
            Main.verboseln("\tGSet " + gSet);
            for (Map<Formula, Integer> ranking : accLocal.accMasterOptions.get(gSet).keySet()) {
                Main.verboseln("\t  Ranking " + ranking);
                TranSet<ProductState> Fin = new TranSet();
                Set<TranSet<ProductState>> Infs = new HashSet();
                Fin.addAll((TranSet<ProductState>) accLocal.accMasterOptions.get(gSet).get(ranking).left);
                for (Formula f : gSet) {
                    GSet localGSet = new GSet(gSet);
                    localGSet.retainAll(accLocal.topmostGs.get(f));
                    RabinPair fPair = accLocal.accSlavesOptions.get(f).get(localGSet).get(ranking.get(f));
                    Fin.addAll((TranSet<ProductState>) fPair.left);
                    Infs.add((TranSet<ProductState>) fPair.right);
                }
                GRabinPairRaw pair = new GRabinPairRaw(Fin, Infs);
                Main.verboseln(pair.toString());
                this.add(pair);
            }
        }
    }

    public AccTGRRaw removeRedundancy() {         //(TranSet<ProductState> allTrans) {
        AccTGRRaw removalPairs;
        AccTGRRaw temp;
        Set<TranSet<ProductState>> copy;
        int phase = 0;
        Main.stopwatchLocal();

        Main.verboseln(phase + ". Raw Generalized Rabin Acceptance Condition\n");
        //Main.verboseln(this.toString());
        printProgress(phase++);

        /* is this duplicate here more efficient? not really
        Main.verboseln(phase + ". Removing (F, I) for which there is a less restrictive (G, J) \n");
        removalPairs = new AccTGRRaw();
        for (GRabinPairRaw pair1 : this) {
            for (GRabinPairRaw pair2 : this) {
                if (!pair1.equals(pair2) && pairSubsumed(pair1, pair2)) {
                    removalPairs.add(pair1);
                    break;
                }
            }
        }
        this.removeAll(removalPairs);
        //Main.verboseln(this.toString());
        printProgress(phase++);
        */

        Main.verboseln(phase + ". Removing (F, {I1,...,In}) with complete F\n");
        removalPairs = new AccTGRRaw();
        for (GRabinPairRaw pair : this) {
            if (pair.left.equals(allTrans)) {
                removalPairs.add(pair);
            }
        }
        this.removeAll(removalPairs);
        //Main.verboseln(this.toString());
        printProgress(phase++);

        Main.verboseln(phase + ". Removing complete Ii in (F, {I1,...,In}), i.e. Ii U F = Q \n");
        temp = new AccTGRRaw();
        for (GRabinPairRaw pair : this) {
            copy = new HashSet(pair.right);
            for (TranSet<ProductState> i : pair.right) {
                TranSet<ProductState> iUf = new TranSet();
                if (iUf.addAll(i).addAll(pair.left).equals(allTrans)) {
                    copy.remove(i);
                    break;
                }
            }
            temp.add(new GRabinPairRaw(pair.left, copy));
        }
        this.clear();
        this.addAll(temp);
        //Main.verboseln(this.toString());
        printProgress(phase++);

        Main.verboseln(phase + ". Removing F from each Ii: (F, {I1,...,In}) |-> (F, {I1\\F,...,In\\F})\n");
        temp = new AccTGRRaw();
        for (GRabinPairRaw pair : this) {
            copy = new HashSet(pair.right);
            for (TranSet<ProductState> i : pair.right) {
                copy.remove(i);         //System.out.println("101:::::::"+i);
                TranSet<ProductState> inew = new TranSet();
                inew.addAll(i);          //System.out.println("105TEMP-BEFORE"+temp+"\n=====");
                inew.removeAll(pair.left); //System.out.println("105TEMP-BETWEEN"+temp+"\n=====");
                copy.add(inew);         //System.out.println("103TEMP-AFTER"+temp);
            }
            temp.add(new GRabinPairRaw(pair.left, copy));//System.out.println("105TEMP-AFTER"+temp+"\n=====");
        }
        this.clear();
        this.addAll(temp);
        //Main.verboseln(this.toString());
        printProgress(phase++);

        Main.verboseln(phase + ". Removing (F, {..., \\emptyset, ...} )\n");
        removalPairs = new AccTGRRaw();
        for (GRabinPairRaw pair : this) {
            for (TranSet<ProductState> i : pair.right) {
                if (i.isEmpty()) {
                    removalPairs.add(pair);
                    break;
                }
            }
        }
        this.removeAll(removalPairs);
        //Main.verboseln(this.toString());
        printProgress(phase++);

        Main.verboseln(phase + ". Removing redundant Ii: (F, I) |-> (F, { i | i in I and !\\exists j in I : Ij <= Ii })\n");
        temp = new AccTGRRaw();
        for (GRabinPairRaw pair : this) {
            copy = new HashSet(pair.right);
            for (TranSet<ProductState> i : pair.right) {
                for (TranSet<ProductState> j : pair.right) {
                    if (!j.equals(i) && j.subsetOf(i)) {
                        copy.remove(i);
                        break;
                    }
                }
            }
            temp.add(new GRabinPairRaw(pair.left, copy));
        }
        this.clear();
        this.addAll(temp);
        //Main.verboseln(this.toString());
        printProgress(phase++);

        Main.verboseln(phase + ". Removing (F, I) for which there is a less restrictive (G, J) \n");
        removalPairs = new AccTGRRaw();
        for (GRabinPairRaw pair1 : this) {
            for (GRabinPairRaw pair2 : this) {
                if (!pair1.equals(pair2) && pairSubsumed(pair1, pair2)) {
                    removalPairs.add(pair1);
                    break;
                }
            }
        }
        this.removeAll(removalPairs);
        //Main.verboseln(this.toString());
        printProgress(phase++);
        return this;
    }

    /**
     * True if pair1 is more restrictive than pair2
     */
    private boolean pairSubsumed(GRabinPairRaw pair1, GRabinPairRaw pair2) {
        if (!pair2.left.subsetOf(pair1.left)) {
            return false;
        }
        for (TranSet<ProductState> i2 : pair2.right) {
            boolean i2CanBeMatched = false;
            for (TranSet<ProductState> i1 : pair1.right) {
                if (i1.subsetOf(i2)) {
                    i2CanBeMatched = true;
                    break;
                }
            }
            if (!i2CanBeMatched) {
                return false;
            }
        }
        return true;
    }

    public void printProgress(int phase) {
        Main.nonsilent("Phase " + phase + ": "
            + Main.stopwatchLocal() + " s " + this.size() + " pairs");
    }

    public String toString() {
        String result = "Gen. Rabin acceptance condition";
        int i = 1;
        for (GRabinPairRaw pair : this) {
            result += "\nPair " + i + "\n" + pair.toString();
            i++;
        }
        return result;
    }

}
