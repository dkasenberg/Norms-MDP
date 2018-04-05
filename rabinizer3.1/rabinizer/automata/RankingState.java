/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rabinizer.automata;

import rabinizer.formulas.Formula;
import java.util.*;

/**
 *
 * @author jkretinsky
 */
public class RankingState extends HashMap<FormulaState,Integer>{
    
    public RankingState(){
        super();
    }
    
    
    public String toString(){
        String result = "";
        for (FormulaState f : keySet()) {
            result += " " + f + "=" + get(f);
        }
        return result;
    }

}
