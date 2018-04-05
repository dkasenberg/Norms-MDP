/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.normconflictresolution;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import normativeagents.actions.RestrictedActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that monitors the environment, and makes sure that the {@link NormConflictResolver} updates the best action
 * at each time step.
 *
 * @author dkasenberg
 */
public class NCREnvironmentObserver implements EnvironmentObserver {
    
    public NormConflictResolver ncr;
    public List<State> stateHistory;
    
    public NCREnvironmentObserver(NormConflictResolver ncr) {
        this.ncr = ncr;
    }
    
    public void reset(State newInitialState) {
        this.stateHistory = new ArrayList<>();
        stateHistory.add(newInitialState);
        ncr.recomputeBestActions(stateHistory);
        updateActions();
        
    }
    
    protected void updateActions() {

        ncr.restricted.domain.getActionTypes().stream().map((a) -> (RestrictedActionType)a).forEach((ra) -> {
            ra.setActionRestriction(ncr.nextActions);
        });
    }

    @Override
    public void observeEnvironmentInteraction(EnvironmentOutcome eo) {
        State sp = eo.op;
        stateHistory.add(sp);
        ncr.recomputeBestActions(stateHistory);
        updateActions();
    }

    @Override
    public void observeEnvironmentReset(Environment resetEnvironment) {
        this.reset(resetEnvironment.currentObservation());
    }
    @Override
    public void observeEnvironmentActionInitiation(State o, Action action) {

    }

}
