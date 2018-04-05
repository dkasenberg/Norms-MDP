/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabinizer.bdd;

import net.sf.javabdd.BDD;
import rabinizer.formulas.BooleanConstant;
import rabinizer.formulas.Disjunction;
import rabinizer.formulas.Formula;
import java.util.*;
import rabinizer.formulas.Literal;

/**
 *
 * @author jkretinsky
 */
public class ValuationSetExplicit extends ValuationSet {

    private Set<Valuation> valuations;

    public ValuationSetExplicit(Globals globals) {
        super(globals);
        valuations = new HashSet();
    }

    public ValuationSetExplicit(ValuationSet vs) {
        super(vs.globals);
        valuations = vs.toSet();
    }
    
    public ValuationSetExplicit(Set<Valuation> vs, Globals globals) {
        super(globals);
        valuations = vs;
    }

    public ValuationSetExplicit(Valuation v, Globals globals) {
        this(globals);
        valuations.add(v);
    }

    @Override
    public Formula toFormula() {
        Formula result = new BooleanConstant(false, globals);
        for (Valuation v : valuations) {
            result = (new Disjunction(result, v.toFormula())).representative();
        }
        return result.representative();
    }

    @Override
    public BDD toBdd() {
        BDD result = globals.bddForVariables.getFalseBDD();
        for (Valuation v : valuations) {
            result = result.or(v.toValuationBDD());
        }
        return result;
    }

    @Override
    public Set<Valuation> toSet() {
        return valuations;
    }

    @Override
    public Valuation pickAny() {
        return valuations.iterator().next();
    }

    @Override
    public ValuationSet add(ValuationSet vs) {
        valuations.addAll(vs.toSet());
        return this;
    }

    @Override
    public ValuationSet add(Valuation v) {
        valuations.add(v);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ValuationSet)) {
            return false;
        } else {
            return ((ValuationSet) o).toSet().equals(this.toSet());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.valuations);
    }

    @Override
    public boolean contains(Valuation v) {
        return valuations.contains(v);
    }

    @Override
    public ValuationSet and(Literal literal) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ValuationSet and(ValuationSet vs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public ValuationSet or(ValuationSet vs) {
        Set<Valuation> union = new HashSet();
        union.addAll(valuations);
        union.addAll(vs.toSet());
        return new ValuationSetExplicit(union, globals);
    }

    @Override
    public boolean isAllVals() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(ValuationSet vs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(ValuationSet vs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ValuationSet complement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
