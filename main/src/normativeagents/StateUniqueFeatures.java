package normativeagents;

import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dkasenberg on 10/28/17.
 */
public class StateUniqueFeatures implements SparseStateFeatures {

        public List<HashableState> allStates;
        public HashableStateFactory hf;

        public StateUniqueFeatures(SADomain d, State initialState, HashableStateFactory hf) {
            allStates = new ArrayList<>(Helper.getAllReachableStates(d,initialState, hf));
            this.hf = hf;
        }

        public StateUniqueFeatures(List<HashableState> allStates) {
            this.allStates = new ArrayList<>(allStates);
        }

        @Override
        public List<StateFeature> features(State state) {
            HashableState hs = hf.hashState(state);
            int index = allStates.indexOf(hs);
            if(index == -1) throw new RuntimeException("State not found.");
            return Collections.singletonList(new StateFeature(allStates.indexOf(hs),1));
        }

        @Override
        public SparseStateFeatures copy() {
            return new StateUniqueFeatures(this.allStates);
        }

        @Override
        public int numFeatures() {
            return allStates.size();
        }
}
