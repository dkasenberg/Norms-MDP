package rabinizer.exec;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * Helper class for transferring general acceptance conditions from Rabinizer to
 * Prism (used in a submission to CAV 2013 and ATVA 2014).
 * 
 * @author andreas
 * 
 */
public class GenAccCondPrism {

	class GRPairPrism {
		public BitSet finSet;
		public ArrayList<BitSet> infSets;
	}

	private final ArrayList<GRPairPrism> conds;

	public GenAccCondPrism() {
		conds = new ArrayList();
	}

	public void addAccCond(BitSet f, ArrayList<BitSet> infs) {
		GRPairPrism c = new GRPairPrism();
		c.finSet = f;
		c.infSets = infs;
		conds.add(c);
	}

	public ArrayList<BitSet> getInf(int i) {
		return conds.get(i).infSets;
	}

	public BitSet getFin(int i) {
		return conds.get(i).finSet;
	}

	public int getNrOfAccConds() {
		return conds.size();
	}

}
