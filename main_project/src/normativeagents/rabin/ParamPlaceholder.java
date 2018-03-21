/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rabin;

import net.sf.javabdd.BDD;
import org.apache.commons.lang3.StringUtils;
import rabinizer.bdd.Globals;
import rabinizer.formulas.Formula;
import rabinizer.formulas.Literal;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public class ParamPlaceholder extends Formula {
    
    protected String propName;
    protected List<String> params;
    protected Map<String, String> applied;
    
    public ParamPlaceholder(String propName, List<String> params, Globals globals) {
        super(globals);
        this.propName = propName;
        this.params = params;
        this.applied = new HashMap<>();
    }
    
    public Formula copy() {
        ParamPlaceholder theCopy = new ParamPlaceholder(propName, new ArrayList<>(params), globals);
        theCopy.applied = new HashMap<>(applied);
        return theCopy;
    }
    
    public Formula applyParam(String param, String value) {
        
        ParamPlaceholder theCopy = (ParamPlaceholder)this.copy();
        theCopy.applied.put(param, value);
        // Check if all variables have been bound
        if(theCopy.applied.keySet().containsAll(params)) {
            return theCopy.applyAllParams();
        }
        
        return theCopy;
    }
    
    protected Formula applyAllParams() {
        // Order must be retained here!
        List<String> actualsAsArray = params.stream().map(param -> applied.get(param)).collect(Collectors.toList());
        String atom = propName + ":" + StringUtils.join(actualsAsArray, ",");
        int id = globals.bddForVariables.bijectionIdAtom.id(atom);
        return new Literal(atom, id, false, globals);        
    }
    
    @Override
    public String operator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BDD bdd() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toReversePolishString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Formula toNNF() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Formula negationToNNF() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsG() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasSubformula(Formula f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Formula> gSubformulas() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Formula> topmostGs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Formula unfold() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Formula unfoldNoG() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
