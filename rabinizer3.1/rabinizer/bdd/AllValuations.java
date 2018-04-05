/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.bdd;

import net.sf.javabdd.*;
import java.util.*;

/**
 *
 * @author jkretinsky
 */
public class AllValuations {

    /**
     * All the possible valuations.
     *
     * Created and populated by initializeValuations.
     */
    public List<Valuation> allValuations;
    public Set<ValuationSet> allValuationsAsSets;

    private void enumerateValuations(boolean[] values, int id, Globals globals) {
        if (id <= 0) {
            values[0] = false;
            allValuations.add(new Valuation(values, globals));
            values[0] = true;
            allValuations.add(new Valuation(values, globals));
        } else {
            values[id] = false;
            enumerateValuations(values, id - 1, globals);
            values[id] = true;
            enumerateValuations(values, id - 1, globals);
        }
    }

    /**
     * Create the valuations corresponding to n variables and store them in
     * valuations. The memory consumption is in the order of 2^n.
     *
     * @param n
     * @return
     */
    public void initializeValuations(int n, Globals globals) {
        allValuations = new ArrayList();
        boolean[] values = new boolean[n];
        enumerateValuations(values, n - 1, globals);
        allValuationsAsSets = new HashSet();
        for(Valuation v:allValuations){
            allValuationsAsSets.add(new ValuationSetExplicit(v, globals));
        }
    }
    
    

    //public static BooleanConstant fTrue = new BooleanConstant(true);
    //public static BooleanConstant fFalse = new BooleanConstant(false);
}
