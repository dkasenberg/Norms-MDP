package rabinizer.formulas;

import net.sf.javabdd.BDD;
import rabinizer.bdd.BDDForFormulae;
import rabinizer.bdd.Globals;

public class BooleanConstant extends FormulaNullary {

    public boolean value;

    public BooleanConstant(boolean value, Globals globals) {
        super(globals);
        this.value = value;
    }

    @Override
    public String operator() {
        return null;
    }

    @Override
    public BDD bdd() {
        if (cachedBdd == null) { 
            cachedBdd = (this.value ? globals.bddForFormulae.bddFactory.one() : globals.bddForFormulae.bddFactory.zero());
            globals.bddForFormulae.representativeOfBdd(cachedBdd, this);
        } 
        return cachedBdd;
    }

    @Override
    public int hashCode() {
        return value ? 0 : 1;
    }

    public Formula copy() {
        return new BooleanConstant(value, globals);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BooleanConstant)) {
            return false;
        } else {
            return ((BooleanConstant) o).value == value;
        }
    }

    @Override
    public String toReversePolishString() {
        return toString();
    }

    @Override
    public String toString() {
        if (cachedString == null) {
            cachedString = (value ? "true" : "false");
        }
        return cachedString;
    }

    @Override
    public Formula negationToNNF() {
        return new BooleanConstant(!value, globals);
    }

}
