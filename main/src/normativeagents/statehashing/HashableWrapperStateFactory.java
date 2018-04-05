package normativeagents.statehashing;

import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

/**
 * Created by dan on 5/18/17.
 */
public class HashableWrapperStateFactory implements HashableStateFactory {

//    /**
//     * Whether state evaluations of OO-MDPs are object identifier independent (the names of objects don't matter). By
//     * default it is independent.
//     */
//    protected boolean identifierIndependent = true;


    /**
     * Default constructor: object identifier independent and no hash code caching.
     */
    public HashableWrapperStateFactory(){

    }

//    /**
//     * Initializes with no hash code caching.
//     * @param identifierIndependent if true then state evaluations for {@link burlap.mdp.core.oo.state.OOState}s are object identifier independent; if false then dependent.
//     */
//    public HashableWrapperStateFactory(){
//        this.identifierIndependent = identifierIndependent;
//    }


    @Override
    public HashableState hashState(State s) {
        if(s instanceof HashableWrapperState){
            return (HashableState)s;
        }

        return new HashableWrapperState(s);
    }


//    public boolean objectIdentifierIndependent() {
//        return this.identifierIndependent;
//    }





}