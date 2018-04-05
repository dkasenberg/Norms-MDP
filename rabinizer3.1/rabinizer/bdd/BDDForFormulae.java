package rabinizer.bdd;

import com.juliasoft.beedeedee.factories.JavaBDDAdapterFactory;
import java.util.*;
import net.sf.javabdd.*;
import rabinizer.formulas.*;

/**
 * Global state & pervasive methods.
 *
 * @author Ruslan Ledesma-Garza
 *
 */
public class BDDForFormulae {

    /**
     * The BDD factory for the purpose of constructing canonical representations
     * of formulas.
     */
    public BDDFactory bddFactory;

    /**
     * The map from boolean atoms to BDD variables for the purpose of
     * constructing BDDs over boolean atoms.
     *
     * Populated by Formula.bdd().
     */
    public BijectionBooleanAtomBddVar bijectionBooleanAtomBddVar;

    public void init() {
        bijectionBooleanAtomBddVar = new BijectionBooleanAtomBddVar();
        bddFactory = JavaBDDAdapterFactory.init("java", 100, 100);
        bddToRepresentative = new HashMap();
    }

    /**
     * Cache for the representative of a given bdd
     */
    private Map<BDD, Formula> bddToRepresentative;

    /**
     * Return the cached representative of a BDD.
     */
    public Formula representativeOfBdd(BDD bdd, Formula representativeCandidate) {
        if (!bddToRepresentative.containsKey(bdd)) {
            bddToRepresentative.put(bdd, representativeCandidate);
        }
        return bddToRepresentative.get(bdd);
    }

    public BDD trueFormulaBDD() {
        return bddFactory.one();
    }

}
