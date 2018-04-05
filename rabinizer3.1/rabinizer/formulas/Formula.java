package rabinizer.formulas;

import rabinizer.bdd.GSet;
import rabinizer.bdd.Valuation;
import rabinizer.bdd.BDDForFormulae;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.sf.javabdd.*;
import rabinizer.bdd.Globals;

/**
 * @author Jan Kretinsky
 *
 */
public abstract class Formula {

    protected String cachedString;
    protected BDD cachedBdd = null;
    public Globals globals;
    
    public Formula(Globals globals) {
        this.globals = globals;
    }
    
    public abstract String operator();

    public abstract BDD bdd();

    public Formula representative() {
        return globals.bddForFormulae.representativeOfBdd(bdd(), this);
    }

    @Override
    public abstract int hashCode();
    
    public List<Formula> generalize(Map<String, String> paramClasses, Set<String> specificObjects, Map<String, List<String>> objects) {
        // Generalize over the first variable and call generalize on the result over the other variables.
        
        List<Formula> toReturn = new ArrayList<>();
        
        if(paramClasses.isEmpty()) {
            toReturn.add(this);
            return toReturn;
        }
        List<Entry<String, String>> entryList = paramClasses.entrySet().stream().collect(Collectors.toList());
        Entry<String, String> entry = entryList.get(0);
        String varName = entry.getKey();
        String varClass = entry.getValue();

        Map<String,String> leftovers = new HashMap<>(paramClasses);
        leftovers.remove(varName);
        Formula f = new BooleanConstant(true, globals);
        
        List<Formula> rest = this.generalize(leftovers, specificObjects, objects);


        if(specificObjects.contains(varName)) {
            toReturn.addAll(rest.stream().map(formula->formula.applyParam(varName,varName)).collect(Collectors.toList()));
            return toReturn;
        }

        for(String obj : objects.get(varClass)) {
            toReturn.addAll(rest.stream().map(formula -> formula.applyParam(varName, obj)).collect(Collectors.toList()));
        }
        return toReturn;
    }
    
    public Formula applyParam(String varName, String obj) {
        return this.copy();
    }
    
    public abstract Formula copy();

    @Override
    public abstract boolean equals(Object o);

    public abstract String toReversePolishString();

    public abstract Formula toNNF();

    public abstract Formula negationToNNF();

    public abstract boolean containsG();

    public abstract boolean hasSubformula(Formula f);

    public abstract Set<Formula> gSubformulas();

    public abstract Set<Formula> topmostGs();

    // unfold everything, used in master automaton
    public abstract Formula unfold();

    // unfold everything but G's, used in slave automata
    public abstract Formula unfoldNoG();

    public Formula temporalStep(Valuation valuation) {
        return this.assertValuation(valuation).removeX();
    }

    public Formula assertValuation(Valuation valuation) {
        return evaluateValuation(valuation).removeConstants();
    }

    public Formula assertLiteral(Literal literal) {
        return evaluateLiteral(literal).removeConstants();
    }

    public Set<Formula> relevantGFormulas(Set<Formula> candidates) { // TODO: is with the outer G (not GSet)
        Set<Formula> result = new HashSet();
        for (Formula subFormula : candidates) {
            if (hasSubformula(subFormula) && !unfold().representative().ignoresG(subFormula)) {
                result.add(subFormula);
            }
        }
        return result;
    }

    // is not recurrent and is not produced by anything recurrent from f
    // currently, the latter is safely approximated by having a different subformula
    public boolean isTransientwrt(Formula f) {
        return !containsG() && isVeryDifferentFrom(f);
    }

    // =============================================================
    // to be overridden by Boolean and Literal
    public Formula evaluateValuation(Valuation valuation) {
        return this;
    }

    // to be overridden by Boolean and Literal
    public Formula evaluateLiteral(Literal literal) {
        return this;
    }

    // to be overridden by Boolean and Literal
    public Literal getAnUnguardedLiteral() {
        return null;
    }

    // to be overridden by Boolean and XOperator
    public Formula removeX() {
        return this;
    }

    // to be overridden by Boolean and GOperator
    public Formula substituteGsToFalse(GSet gSet) {
        return this;
    }

    // to be overridden by Boolean
    public Formula removeConstants() {
        return this;
    }

    // to be overridden by Boolean
    // contains a modal/Literal not contained in f
    public boolean isVeryDifferentFrom(Formula f) {
        return !f.hasSubformula(this);
    }

    // to be overridden by Boolean
    public boolean ignoresG(Formula f) {
        //return false;
        return !hasSubformula(f);
    }

    // to be overridden by Disjunction
    public boolean isUnfoldOfF() {
        return false;
    }

}
