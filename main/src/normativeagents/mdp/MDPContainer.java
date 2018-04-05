/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.mdp;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;

/**
 *
 * @author dkasenberg
 */
public class MDPContainer {
    public SADomain domain;
    public State initialState;
    public HashableStateFactory hashingFactory;

    
    public MDPContainer(SADomain domain,
            State s0, HashableStateFactory hf) {
        this.hashingFactory = hf;
        this.domain = domain;
        this.initialState = s0;
    }
    
    public MDPContainer() {}
}
    
    
