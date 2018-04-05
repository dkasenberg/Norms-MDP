package rabinizer.bdd;

import rabinizer.exec.*;
import java.util.*;
import rabinizer.formulas.*;

/**
 *
 * @author Andreas Gaiser & Ruslan Ledesma-Garza
 *
 */
public class BijectionBooleanAtomBddVar {

    Map<Formula, Integer> atomToId;
    Map<Integer, Formula> idToAtom;
    
    public BijectionBooleanAtomBddVar() {
        atomToId = new HashMap();
        idToAtom = new HashMap();
    }

    /**
     * The corresponding identifier to atom. If atom is not stored, return an identifier
     * that is fresh for this instance.
     *
     * @param atom
     * @return
     */
    public int id(Formula atom) {
        Integer id = atomToId.get(atom);
        if (id == null) {
            int nextId = idToAtom.size();
            idToAtom.put(nextId, atom);
            atomToId.put(atom, nextId);
            return nextId;
        } else {
            return id;
        }
    }

    public Formula atom(int id) {
        return idToAtom.get(id);
    }

    public int size() {
        return idToAtom.size();
    }

    @Override
    public String toString() {
        return idToAtom.toString();
    }
}
