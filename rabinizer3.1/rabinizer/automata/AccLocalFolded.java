/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.automata;

import rabinizer.bdd.GSet;
import java.util.Map;
import static rabinizer.automata.AccLocal.entails;
import rabinizer.bdd.Globals;
import rabinizer.bdd.Valuation;
import rabinizer.bdd.ValuationSetBDD;
import rabinizer.formulas.*;

/**
 *
 * @author jkretinsky
 */
public class AccLocalFolded extends AccLocal {

    public AccLocalFolded(Product product) {
        super(product);
    }

    @Override
    protected TranSet<ProductState> computeAccMasterForState(GSet gSet, GSet gSetComplement, Map<Formula, Integer> ranking, ProductState ps) {
        TranSet<ProductState> result = new TranSet();
        Globals g = ps.masterState.formula.globals;
        if (!slavesEntail(gSet, gSetComplement, ps, ranking, null, ps.masterState.formula)) {
            result.add(ps, g.vsBDD.getAllVals());
        }
        return result;
    }

    protected static boolean slavesEntail(GSet gSet, GSet gSetComplement, ProductState ps, Map<Formula, Integer> ranking, Valuation v, Formula consequent) {
        Formula antecedent = new BooleanConstant(true, consequent.globals);
        for (Formula f : gSet) {
            antecedent = new Conjunction(antecedent, new GOperator(f)); // TODO relevant for Folded version
            //antecedent = new Conjunction(antecedent, new XOperator(new GOperator(f))); // TODO:remove; relevant for Xunfolding
            Formula slaveAntecedent = new BooleanConstant(true, consequent.globals);
            if (ps.containsKey(f)) {
                for (FormulaState s : ps.get(f).keySet()) {
                    if (ps.get(f).get(s) >= ranking.get(f)) {
                        slaveAntecedent = new Conjunction(slaveAntecedent, s.formula);
                    }
                }
            }
            slaveAntecedent = slaveAntecedent.substituteGsToFalse(gSetComplement);
            antecedent = new Conjunction(antecedent, slaveAntecedent);
        }
        return entails(antecedent, consequent);
    }

}
