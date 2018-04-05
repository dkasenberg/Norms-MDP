package normativeagents.mdp;

/**
 * Created by dan on 5/16/17.
 */
public class WrappedMDPContainer extends MDPContainer {
    public MDPContainer mdp;

    public WrappedMDPContainer(MDPContainer mdp) {
        this.hashingFactory = mdp.hashingFactory;
        this.domain = mdp.domain;
        this.initialState = mdp.initialState.copy();
        this.mdp = mdp;
    }

}
