package rabinizer.formulas;

import net.sf.javabdd.*;
import rabinizer.bdd.BDDForFormulae;

/**
 * @author Andreas Gaiser & Ruslan Ledesma-Garza
 *
 */
public class Conjunction extends FormulaBinaryBoolean {

    public Conjunction(Formula left, Formula right) {
        super(left, right);
    }

    @Override
    public Conjunction ThisTypeBoolean(Formula left, Formula right) {
        return new Conjunction(left, right);
    }

    @Override
    public String operator() {
        return "&";
    }
    
    public Formula copy() {
        return new Conjunction(left.copy(), right.copy());
    }
    
    public Formula applyParam(String k, String v) {
        return new Conjunction(left.applyParam(k, v), right.applyParam(k, v));
    }

    @Override
    public BDD bdd() {
        if (cachedBdd == null) {
            cachedBdd = left.bdd().and(right.bdd());
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
                return new BooleanConstant(false, globals);
            }
        } else {
            Formula new_right = right.removeConstants();
            if (new_right instanceof BooleanConstant) {
                if (((BooleanConstant) new_right).value) {
                    return new_left;
                } else {
                    return new BooleanConstant(false, globals);
                }
            } else {
                return new Conjunction(new_left, new_right);
            }
        }
    }

    @Override
    public boolean ignoresG(Formula f) {
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
        if (!hasSubformula(f) || left.isTransientwrt(right) || right.isTransientwrt(left)) {
            return true;
        } else {
            return left.ignoresG(f) && right.ignoresG(f); 	// don't know yet
        }
    }

    @Override
    public Formula toNNF() {
        return new Conjunction(left.toNNF(), right.toNNF());
    }

    @Override
    public Formula negationToNNF() {
        return new Disjunction(left.negationToNNF(), right.negationToNNF());
    }

}
