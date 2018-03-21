package normativeagents.single.domains.shopworld;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import normativeagents.single.domains.shopworld.state.ShopWorldState;
import normativeagents.single.domains.shopworld.state.ShopWorldTrinket;

import java.util.ArrayList;
import java.util.List;

import static normativeagents.single.domains.shopworld.ShopWorldDomain.*;

/**
 * Created by dan on 5/15/17.
 */
public class ShopWorldModel implements FullStateModel {

    public ShopWorldModel() {

    }

    @Override
    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        if(!(s instanceof ShopWorldState)) throw new RuntimeException("Can't use this model for non-ShopWorld state");
        ShopWorldState st = (ShopWorldState)s.copy();
        String aname = a.actionName();
        if(aname.equals(ACTION_LEAVE)) {
            return leave(st, (ObjectParameterizedAction)a);
        }

        return FullStateModel.Helper.deterministicTransition(this, s, a);
    }

    private List<StateTransitionProb> leave(ShopWorldState s, ObjectParameterizedAction a) {
        List <StateTransitionProb> transitions = new ArrayList<>();


        double totalProbability = 0.0;
        double multiplier = 1.0;
            for(ObjectInstance trinket : s.objectsOfClass(CLASS_TRINKET)) {
                double probOfCatching = 0.0;
                if((Boolean)trinket.get(VAR_HELD)) {
                    ShopWorldTrinket.Size size = (ShopWorldTrinket.Size)trinket.get(VAR_SIZE);
                    if(size == ShopWorldTrinket.Size.SMALL) {
                        probOfCatching = 0.3; // Allow the user to set these values
                    } else if(size == ShopWorldTrinket.Size.MEDIUM) {
                        probOfCatching = 0.5;
                    } else if(size == ShopWorldTrinket.Size.LARGE) {
                        probOfCatching = 0.7;
                    }

                    if((Boolean)trinket.get(VAR_HIDDEN)) {
                        probOfCatching *= 0.5;
                    }
                }
                totalProbability += multiplier*probOfCatching;
                multiplier *= (1.0 - probOfCatching);
            }

            ShopWorldState nocatch = (ShopWorldState)s.copy();
            nocatch.touchAgent().leftStore = true;
            transitions.add(new StateTransitionProb(nocatch, 1.0 - totalProbability));

            ShopWorldState yescatch = (ShopWorldState)s.copy();
            yescatch.touchAgent().leftStore = true;
            yescatch.touchAgent().caughtStealing = true;
            transitions.add(new StateTransitionProb(yescatch, totalProbability));

            return transitions;
    }

    @Override
    public State sample(State s, Action a) {
        if(!(s instanceof ShopWorldState)) throw new RuntimeException("Can't use this model for non-ShopWorld state");
        ShopWorldState st = (ShopWorldState)s.copy();
        String aname = a.actionName();
        if(aname.equals(ACTION_LEAVE)) {
            return FullStateModel.Helper.sampleByEnumeration(this,s,a);
        }
        else if(aname.equals(ACTION_PICKUP)) {
            return pickUp(st, (ObjectParameterizedAction)a);
        }
        else if(aname.equals(ACTION_PUTDOWN)) {
            return putDown(st, (ObjectParameterizedAction)a);
        }
        else if(aname.equals(ACTION_BUY)) {
            return buy(st, (ObjectParameterizedAction)a);
        }
        else if(aname.equals(ACTION_HIDE)) {
            return hide(st, (ObjectParameterizedAction)a);
        }
        else if(aname.equals(ACTION_NOOP)) {
            return noop(st, (ObjectParameterizedAction)a);
        }

        throw new RuntimeException("Unrecognized action.");
    }

    private State noop(ShopWorldState s, ObjectParameterizedAction a) {
        return s;
    }

    private State hide(ShopWorldState s, ObjectParameterizedAction a) {
        String [] params = a.getObjectParameters();

        ShopWorldTrinket trinket = (ShopWorldTrinket)s.object(params[0]);

        ShopWorldTrinket nTrinket = trinket.copy();
        nTrinket.hidden = true;

        s.addObject(nTrinket);

        return s;
    }

    private State buy(ShopWorldState s, ObjectParameterizedAction a) {
        String [] params = a.getObjectParameters();

        ShopWorldTrinket trinket = (ShopWorldTrinket)s.object(params[0]);

        ShopWorldTrinket nTrinket = trinket.copy();
        nTrinket.held = false;
        nTrinket.hidden = false;
        nTrinket.bought = true;

        s.addObject(nTrinket);

        s.touchAgent().money = s.agent.money - nTrinket.cost;

        return s;
    }

    private State putDown(ShopWorldState s, ObjectParameterizedAction a) {
        String [] params = a.getObjectParameters();

        ShopWorldTrinket trinket = (ShopWorldTrinket)s.object(params[0]);

        ShopWorldTrinket nTrinket = trinket.copy();
        nTrinket.held = false;
        nTrinket.hidden = false;
        nTrinket.inStock = true;

        s.addObject(nTrinket);

        return s;
    }

    private State pickUp(ShopWorldState s, ObjectParameterizedAction a) {
        String [] params = a.getObjectParameters();

        ShopWorldTrinket trinket = (ShopWorldTrinket)s.object(params[0]);

        ShopWorldTrinket nTrinket = trinket.copy();
        nTrinket.held = true;
        nTrinket.hidden = false;
        nTrinket.inStock = false;

        s.addObject(nTrinket);

        return s;
    }
}
