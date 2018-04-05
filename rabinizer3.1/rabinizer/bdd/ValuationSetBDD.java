/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.bdd;

import java.util.*;
import net.sf.javabdd.BDD;
import rabinizer.formulas.Formula;
import rabinizer.formulas.Literal;

/**
 *
 * @author jkretinsky
 */
public class ValuationSetBDD extends ValuationSet {

    protected BDD valuations;

    public ValuationSetBDD(Globals globals) {
        super(globals);
    }
    
    public ValuationSetBDD(BDD bdd, Globals globals) {
        super(globals);
        valuations = bdd;
    }

    public ValuationSetBDD(ValuationSet vs) {
        super(vs.globals);
        valuations = vs.toBdd();
    }

    @Override
    public Formula toFormula() {
        return new MyBDD(valuations, true, globals).BDDtoFormula();
    }

    @Override
    public BDD toBdd() {
        return valuations;
    }

    @Override
    public Set<Valuation> toSet() {
        Set<Valuation> vs = new HashSet();
        for (Valuation v : globals.aV.allValuations) {
            if (this.contains(v)) {
                vs.add(v);
            }
        }
        return vs;
    }

    @Override
    public Valuation pickAny() {
        Valuation val = new Valuation(globals.bddForVariables.numOfVariables, globals);
        BDD b = valuations;
        while (!b.isOne()) {
            if (!b.high().isZero()) {
                val.set(b.var(), true);
                b = b.high();
            } else {
                val.set(b.var(), false);
                b = b.low();
            }
        }
        return val;
    }

    @Override
    public ValuationSet add(ValuationSet vs) {
        valuations = valuations.or(vs.toBdd());
        return this;
    }
        
    @Override
    public ValuationSet add(Valuation v) {
        valuations = valuations.or(v.toValuationBDD());
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ValuationSet)) {
            return false;
        } else {
            return ((ValuationSet) o).toBdd().biimp(valuations).isOne();
        }
    }

    @Override
    public boolean isAllVals() {
        return valuations.isOne();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.valuations);
    }

    @Override
    public boolean contains(Valuation v) {
        return !valuations.and(v.toValuationBDD()).isZero();
    }

    @Override
    public boolean contains(ValuationSet vs) {
        return valuations.not().and(vs.toBdd()).isZero();
    }

    @Override
    public boolean isEmpty() {
        return valuations.isZero();
    }

    @Override
    public ValuationSet and(Literal literal) {
        BDD res = valuations.and(globals.bddForVariables.literalToBDD(literal));
        ValuationSet result = new ValuationSetBDD(res, globals);
        return result;
    }

    @Override
    public ValuationSet and(ValuationSet vs) {
        return new ValuationSetBDD(valuations.and(vs.toBdd()), globals);
    }
    
    @Override
    public ValuationSet or(ValuationSet vs) {
        return new ValuationSetBDD(valuations.or(vs.toBdd()), globals);
    }
    
    @Override
    public ValuationSet complement() {
        return new ValuationSetBDD(valuations.not(), globals);
    }

    @Override
    public void remove(ValuationSet vs) {
        valuations = valuations.and(vs.toBdd().not());
    }
    
    public ValuationSetBDD getAllVals(){
        return new ValuationSetBDD(globals.bddForVariables.getTrueBDD(), globals);
    }

}
