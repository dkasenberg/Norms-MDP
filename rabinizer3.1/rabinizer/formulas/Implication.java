package rabinizer.formulas;

import net.sf.javabdd.*;
import rabinizer.bdd.BDDForFormulae;

/**
 * @author Andreas Gaiser & Ruslan Ledesma-Garza
 *
 */
public class Implication extends FormulaBinaryBoolean {

    public Implication(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public Implication ThisTypeBoolean(Formula left, Formula right) {
        return new Implication(left, right);
    }

    @Override
    public String operator() {
        return "->";
    }
    
    public Formula copy() {
        return new Implication(left.copy(), right.copy());
    }
    
    public Formula applyParam(String k, String v) {
        return new Implication(left.applyParam(k, v), right.applyParam(k, v));
    }

    @Override
    public BDD bdd() {
        if (cachedBdd == null) {
            cachedBdd = left.bdd().imp(right.bdd());
            globals.bddForFormulae.representativeOfBdd(cachedBdd, this);
        }
        return cachedBdd;
    }

    @Override
    public Formula removeConstants() {
        Formula new_left = left.removeConstants();
        if (new_left instanceof BooleanConstant) {
            if (((BooleanConstant) new_left).value) {
                return right.removeConstants();
            } else {
                return new BooleanConstant(true, globals);
            }
        } else {
            Formula new_right = right.removeConstants();
            if (new_right instanceof BooleanConstant) {
                if (((BooleanConstant) new_right).value) {
                    return new BooleanConstant(true, globals);
                } else {
                    return new Negation(new_left);
                }
            } else {
                return new Implication(new_left, new_right);
            }
        }
    }

    @Override
    public boolean ignoresG(Formula f) {
        Formula equivalent = new Negation(new Conjunction(left, new Negation(right)));
        return equivalent.ignoresG(f);
//        if (!left.isVeryDifferentFrom(right)) {
//            //System.out.println("$$$$$$not very different$$$$"+this);
//            return false;
//        }
//        if (left.equals(f.unfold()) && right.isUnfoldOfF() && !right.containsG()
//            || right.equals(f.unfold()) && left.isUnfoldOfF() && !left.containsG()) {
//            //System.out.println("$$$$$$"+left+"$$$$"+right+"$$$susp "+f);
//            return true;					// independent waiting formula
//        } else {
//            return left.ignoresG(f) && right.ignoresG(f); 	// don't know yet
//        }
//        if (!hasSubformula(f) || left.isTransientwrt(right) || right.isTransientwrt(left)) {
//            return true;
//        } else {
//            return left.ignoresG(f) && right.ignoresG(f); 	// don't know yet
//        }
    }

    @Override
    public Formula toNNF() {
        Formula equivalent = new Negation(new Conjunction(left, new Negation(right)));
        return equivalent.toNNF();
    }

    @Override
    public Formula negationToNNF() {
        Formula equivalent = new Negation(new Conjunction(left, new Negation(right)));
        return equivalent.negationToNNF();
    }

}
