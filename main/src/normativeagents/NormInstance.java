/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents;

import normativeagents.parsing.LTLNorm;
import normativeagents.rabin.CRDRA;
import normativeagents.rabin.RabinAutomaton;
import rabinizer.formulas.Formula;

/**
 *
 * @author dkasenberg
 */
public class NormInstance {
    public Formula formula;
    public double weight;
    public RabinAutomaton dra;
    public CRDRA crdra;
    public LTLNorm norm;

    public NormInstance(Formula instance, LTLNorm norm) {
        this(instance, 1.0, norm);
    }
    
    public NormInstance(Formula instance, double weight, LTLNorm norm) {
//        this.formula = new GOperator(instance).removeConstants().toNNF().removeConstants().toNNF();
        this.formula = instance.removeConstants().toNNF().removeConstants().toNNF();
        this.weight = weight;
        this.norm = norm;
    }

    @Override
    public String toString() {
        return formula.toString();
    }

    public void setCRDRA(CRDRA wnra) {
        this.crdra = wnra;
        wnra.normInstance = this;
        wnra.norm = norm;
        if(this == norm.normInstances.get(0)){
            norm.setWNRASize(wnra.numbersToStates.size() + 2);
        }
    }
    
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public Formula toSingleFormula() {
        return formula.copy();
    }

    @Override
    public int hashCode() {
        return formula.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NormInstance)) return false;
        return this.formula.equals(((NormInstance) obj).formula);
    }

}
