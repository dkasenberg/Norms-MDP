/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.parsing;

import normativeagents.NormInstance;
import rabinizer.formulas.BooleanConstant;
import rabinizer.formulas.Conjunction;
import rabinizer.formulas.Formula;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public class LTLNorm {
    public Formula formula;
    public List<NormInstance> normInstances;
    public boolean isNumerical;
    public int wnraSize;
        
    public LTLNorm(Formula form, Map<String, String> normParams, Set<String> specificObjects, Map<String, List<String>> objs) {
        this.formula = form;
        List<Formula> instances = formula.generalize(normParams, specificObjects, objs);
        this.normInstances = instances.stream()
                .map(i -> new NormInstance(i.toNNF(), this)).collect(Collectors.toList());
        this.isNumerical = false;
    }

    @Override
    public String toString() {
        return normInstances.toString();
    }
    
    public void setWeight(double weight) {
        this.normInstances.stream().forEach(ni -> ni.setWeight(weight));
    }
    
    public Formula toSingleFormula() {
        return normInstances.stream()
                .map(ni -> ni.toSingleFormula())
                .reduce(new BooleanConstant(true, formula.globals), 
                        (f1, f2) -> new Conjunction(f1, f2));
    }

    public void setWNRASize(int wnraSize) {
//        this.isNumerical = false;
        this.isNumerical = (wnraSize < normInstances.size());
//        this.isNumerical = true;
        this.wnraSize = wnraSize;
    }

    @Override
    public int hashCode() {
        return new HashSet<>(normInstances).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof LTLNorm)) return false;
        LTLNorm nOther = (LTLNorm)other;
        return new HashSet<>(normInstances).equals(new HashSet<>(nOther.normInstances));
    }
}
