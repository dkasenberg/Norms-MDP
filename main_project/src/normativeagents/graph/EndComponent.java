package normativeagents.graph;

import burlap.mdp.core.action.Action;
import burlap.statehashing.HashableState;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an end component: a tuple (S, A) where S is a set of states and A a set of mapping from those states to
 * actions.  If an agent begins in some state s in S and only performs actions in A(s), they are guaranteed to stay
 * within S.  This is important for the end-behavior of the product MDP.
 * @author dkasenberg
 */
public class EndComponent {
    public Set<HashableState> states;
    public Map<HashableState, List<Action>> actions;
}
