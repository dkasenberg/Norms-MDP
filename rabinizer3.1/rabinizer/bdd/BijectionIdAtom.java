package rabinizer.bdd;

import java.util.*;

/**
 * 
 * @author Andreas Gaiser & Ruslan Ledesma-Garza
 * 
 */
public class BijectionIdAtom {

	private Map<Integer, String> idToAtom;
	private Map<String, Integer> atomToId;

	public BijectionIdAtom() {
		atomToId = new HashMap();
		idToAtom = new HashMap();
	}

	/**
	 * The id corresponding to atom. If the atom is not stored, return an identifier that is fresh for this bijection.
	 * 
	 * @param atom
	 * @return
	 */
	public int id(String atom) {
		Integer id = atomToId.get(atom);
		if (id == null) {
			int nextId = idToAtom.size();
			idToAtom.put(nextId, atom);
			atomToId.put(atom, nextId);
			return nextId;
		} else
			return id;
	}

	public String atom(int id) {
		return idToAtom.get(id);
	}

	/**
	 * The size of the bijection.
	 * 
	 * @return
	 */
	public int size() {
		return idToAtom.size(); // = counter/2
	}
	
        @Override
	public String toString() {
		return idToAtom.toString();
	}
	
}
