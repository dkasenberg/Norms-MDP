/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.normconflictresolution;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.debugtools.DPrint;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.SimpleAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.Helper;
import normativeagents.NormInstance;
import normativeagents.RandomPolicyMod;
import normativeagents.RestrictedRandomPolicy;
import normativeagents.actions.CRDRAAction;
import normativeagents.actions.RestrictedActionType;
import normativeagents.graph.EndComponent;
import normativeagents.graph.EndComponentFinder;
import normativeagents.mdp.CRDRAProductMDP;
import normativeagents.mdp.MDPContainer;
import normativeagents.mdp.RecordedActionMDP;
import normativeagents.mdp.RestrictedMDP;
import normativeagents.mdp.model.CRDRAProductModel;
import normativeagents.mdp.state.CRDRAProductState;
import normativeagents.mdp.state.WrapperState;
import normativeagents.misc.Pair;
import normativeagents.misc.Triple;
import normativeagents.parsing.LTLNorm;
import normativeagents.parsing.NormParser;
import normativeagents.rabin.CRDRA;
import normativeagents.rabin.RabinAutomaton;
import normativeagents.rewardvector.*;
import normativeagents.rewardvector.comparator.WeightedSumComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import rabinizer.bdd.*;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import static normativeagents.actions.CRDRAAction.BREAKONCE;
import static normativeagents.actions.CRDRAAction.GIVEUP;

/**
 *
 * The core class for the norm conflict resolution algorithm (also used in norm inference and NCR).
 * Reads in a string representation of norms, and carries out the norm conflict resolution algorithm
 * @author dkasenberg
 */
public class NormConflictResolver {
    public List<NormInstance> normInstances;
    public List<LTLNorm> norms;
    private MDPContainer origMDP;
    MDPContainer restricted;
    protected HashableStateFactory hashingFactory;
    protected NCREnvironmentObserver observer;
    List<Action> nextActions;
    protected Globals globals;

    private double riskParam;
    public double discount;
    public Map<Pair<HashableState,HashableState>,RealVector> weightedGraph;
    public CRDRAProductMDP product;
    private Map<HashableState, Policy> bestPolicies;
    private Map<HashableState, RealVector> initialValues;
    private Map<HashableState, Set<Action>> bestActions;
    public Set<HashableState> noUpdate;
    private VectorPolicyEvaluation rpValues;

    public RVValueIteration stateValues;

    private Log log = LogFactory.getLog(NormConflictResolver.class);

    /**
     * @param normText a string representation of the given norms.
     * @param d the underlying MDP's domain
     * @param s the start state of the underlying MDP.
     * @param hf A HashableStateFactory for state hashing.
     * @param discount a discount factor in [0,1)
     * */
    public NormConflictResolver(String normText, SADomain d, State s, HashableStateFactory hf, double discount) {
        this(normText, d, s, hf, discount,true);
    }

    /**
     * @param normText a string representation of the given norms.
     * @param d the underlying MDP's domain
     * @param s the start state of the underlying MDP.
     * @param hf A HashableStateFactory for state hashing.
     * @param discount a discount factor in [0,1)
     * @param init whether to immediately plan (otherwise, wait until the weights are reset)
     * */
    public NormConflictResolver(String normText, SADomain d, State s, HashableStateFactory hf, double discount,
                                boolean init) {
        this(normText, d, s, hf, discount, init, new HashSet<>());
    }

    /**
     * @param normText a string representation of the given norms.
     * @param d the underlying MDP's domain
     * @param s the start state of the underlying MDP.
     * @param hf A HashableStateFactory for state hashing.
     * @param discount a discount factor in [0,1)
     * @param parsedNorms a set of previously-parsed norms, if any
     * */
    public NormConflictResolver(String normText, SADomain d, State s, HashableStateFactory hf, double discount,
                                Collection<LTLNorm> parsedNorms) {
        this(normText, d, s, hf, discount, true,  parsedNorms);
    }

    /**
     * Fully specified constructor.
     * @param normText a string representation of the given norms.
     * @param d the underlying MDP's domain
     * @param s the start state of the underlying MDP.
     * @param hf A HashableStateFactory for state hashing.
     * @param discount a discount factor in [0,1)
     * @param init whether to immediately plan (otherwise, wait until the weights are reset)
     * @param parsedNorms a set of previously-parsed norms, if any
     * */
    public NormConflictResolver(String normText, SADomain d, State s, HashableStateFactory hf, double discount,
                                boolean init, Collection<LTLNorm> parsedNorms) {
        norms = new ArrayList<>(parsedNorms);
        origMDP = new MDPContainer(d,s,hf);

        origMDP = new RecordedActionMDP(origMDP);


//        System.err.println(Helper.getAllReachableStates(origMDP.domain, origMDP.initialState, hf).size());

//        This stuff is about multi-threading (if several NormConflictResolvers are created in different threads).
//        Rabinizer normally uses just one set of these variables, but various race conditions made several copies
//        necessary.
        globals = new Globals();
        globals.bddForFormulae = new BDDForFormulae();
        globals.bddForVariables = new BDDForVariables();
        globals.vsBDD = new ValuationSetBDD(globals);
        globals.aV = new AllValuations();



        try {

            if(normText != null) {
                NormParser normParser = new NormParser(globals, new StringReader(normText));
                norms.addAll(normParser.parse(d, s));
            }
//            norms = normParser.parse(d,s);
            normInstances = norms.stream().flatMap(n -> n.normInstances.stream()).collect(Collectors.toList());
        } catch(Exception e) {
            System.err.println("An error occurred while attempting to parse norms.");
            e.printStackTrace();
        }

        globals.bddForVariables.init();
        globals.aV.initializeValuations(globals.bddForVariables.bijectionIdAtom.size(), globals);
        globals.bddForFormulae.init();

        // Give norm instances their own DRAs from the outset.
        normInstances.forEach(ni -> {
            try {
                ni.dra = new RabinAutomaton(ni.toSingleFormula(), origMDP.domain, origMDP.initialState);
            } catch(Exception e) {
                System.err.println("Error generating DRA");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        });

        this.hashingFactory = hf;


        restricted = new RestrictedMDP(origMDP);


        nextActions = Helper.getAllActions(restricted.domain, restricted.initialState);

        observer = new NCREnvironmentObserver(this);

        Logger.getLogger(NormConflictResolver.class).setLevel(Level.WARN);
        this.discount = discount;
        normInstances.forEach(ni -> ni.setCRDRA(new CRDRA(ni.dra, ni.weight, discount)));
        this.riskParam = riskParam;

        if(init) {
            plan();
        }
    }

    /** Figures out which of the given end components are AMECs.
     * @param crdras the set of conflict resolution DRAs
     * @param ecs the set of end components
     * @return The set of end of AMECs.
     * */
    private static Set<EndComponent> filterByAccepting(List<CRDRA> crdras, Set<EndComponent> ecs) {

        // A good end component according to a list of WNRAs is one which is accepting for each WNRA.
        // A good end component according to a WNRA is one such that for some (F_i, I_i) it contains none
        // of the states in F_i and at least one of the states in I_i.

        return ecs.stream().filter(ec -> {
            for(int wnraindex = 0; wnraindex < crdras.size(); wnraindex++) {
                CRDRA wnra = crdras.get(wnraindex);
                final int i = wnraindex;
                List<Set<Integer>> acceptingSets = wnra.accSR;
                boolean wnraSatisfied = false;
                for(int pairIndex = 0; pairIndex < acceptingSets.size(); pairIndex+= 2) {
                    Set<Integer> fins = acceptingSets.get(pairIndex);
                    Set<Integer> infins = acceptingSets.get(pairIndex+1);
                    if(ec.states.stream().noneMatch(hs ->
                       fins.contains(((CRDRAProductState)hs.s()).qs.get(i))
                    )
                            &&
//                    if(
                       ec.states
                               .stream()
                               .anyMatch(hs ->
                                       infins
                                               .contains(((CRDRAProductState)hs.s()).qs.get(i)
                                               ))
                            ) {
                        wnraSatisfied = true;
                        break;
                    }
                }
                if(!wnraSatisfied) return false;
            }
            return true;
        }).collect(Collectors.toSet());
    }

    /**
     * Returns the set of norms as LTLNorm objects.
     * @return The set of LTLNorm objects associated with this {@link NormConflictResolver}.
     * */
    public Collection<LTLNorm> getNorms() {
        return this.norms;
    }

    /**
     * A simple method to initialize this - meant to be extended in child classes
     * */
    public void initialize() {
        observer.reset(origMDP.initialState);
    }

    /**
     * Modify the weights of this {@link NormConflictResolver} and re-plan.
     * @param weights the new set of weights.
     * */
    public void resetWeights(RealVector weights) {
        for(int i =0; i < weights.getDimension(); i++) {
            normInstances.get(i).weight = weights.getEntry(i);
        }
        normInstances.forEach(ni -> ni.setCRDRA(new CRDRA(ni.dra, ni.weight, discount)));
        plan();
    }

    /**
     * Get the start state of this object in the restricted domain (the one in which the actions are restricted to the
     * best norm-following actions).
     * @return the restricted domain's start state.
     * */
    public State getStartState() {
        return this.restricted.initialState;
    }

    /**
     * Get the domain of the underlying MDP (with actions restricted to those permissible).
     * @return the restricted domain.
     */
    public SADomain getCurrentDomain() {
        return this.restricted.domain;
    }

    /**
     * Get an environment in which to run the norm-following agent.
     * @return an {@link Environment} in which to run any agent based on this object.
     * */
    public Environment getEnvironment() {
        return new EnvironmentServer(new SimulatedEnvironment(getCurrentDomain(), getStartState()),this.observer);
    }

    /**
     * The core planning method.  Follows the algorithm in our paper "Norm Conflict Resolution in Stochastic Domains"
     * to determine the optimal courses of action in all states of the product MDP, which can then be used to choose
     * norm-optimal actions.
     * */
    private void plan() {

        double delta = 0.01;
        int maxiters = 1000;

        List<CRDRA> crdraList = normInstances.stream().map(ni -> ni.crdra).collect(Collectors.toList());

        Comparator<RealVector> comparator = new WeightedSumComparator(crdraList,0.0001);

//        This hack is frankly bizarre.  Turns out that things don't work if you don't reference w.accSR somewhere up here...
//        This is why the Log4j level changed whether the algorithm worked - apparently whether the "log.info(...w.accSR)" ran
//        affected whether the algorithm converged.
        crdraList.forEach(w->{
            Object o = w.accSR;
        });

        crdraList.forEach(w-> log.info("CRDRA: " + w.accSR));
        crdraList.forEach(w-> log.info(w.weight));
        
        product = new CRDRAProductMDP(origMDP, norms, crdraList, globals);
        
        OOSADomain tempD = (OOSADomain)product.domain;
        
        State tempS = product.initialState;

//        Get the AMECs of the product state.

        EndComponentFinder amecFinder = new EndComponentFinder();
        Set<EndComponent> endComponents = amecFinder.getMaximalEndComponents(tempD, tempS,
                hashingFactory);
        log.debug("TOTAL # END COMPONENTS: " + endComponents.size());

        Set<EndComponent> accComponents = filterByAccepting(crdraList, endComponents);

        log.debug("TOTAL # AMECs: " + accComponents.size());

//        Print AMEC information if debugging.
        accComponents.forEach(ec->{
            log.debug("---NEXT AMEC---");
            log.debug("AMEC SIZE: " + ec.states.size());
            ec.states.forEach(s->{
                log.debug("-----NEXT STATE");
                log.debug(s.s());
                ec.actions.get(s).forEach(a-> log.debug(a));
            });

        });
        
        Map<HashableState, List<Action>> A = new HashMap<>();
        Set<HashableState> S = Helper.getAllReachableStates(tempD, tempS, hashingFactory);

        log.debug("Total number of product states: " + S.size());

        for(HashableState s : S) {
            A.put(s, Helper.getAllActions(tempD, s.s()));
        }
        CRDRAProductModel model = (CRDRAProductModel)((FactoredModel)tempD.getModel()).getStateModel();

//        Convert the product MDP into a graph with weights (corresponding to the costs of self-transitions in the
//        CRDRA.
        weightedGraph = S.stream()
                .flatMap(s -> A.get(s).stream()
                        .flatMap(a -> model.stateTransitions(s.s(),a).stream()
                        .filter(tp -> tp.p > 0.0)
                        .map(tp -> new Triple<>(s, a, hashingFactory.hashState(tp.s)))))
                .collect(Collectors.toMap(t -> new Pair<>(t.getLeft(), t.getRight()), t -> {
                    CRDRAAction ga = (CRDRAAction)t.getMiddle();
                    HashableState s = t.getLeft();
                    HashableState sp = t.getRight();
                    int[] settings = ga.getSettings();
                    int settingsCounter = 0;

                    RealVector totalWeight = new ArrayRealVector(crdraList.size());

                    for(int i = 0; i < crdraList.size(); i++) {
                        LTLNorm norm = crdraList.get(i).norm;
                        double breakOnceWeight = 1.0;
                        double giveUpWeight = 1.0/(1.0 - discount);
                        if(norm.isNumerical) {
                            CRDRAProductState ps = (CRDRAProductState)s.s();
                            for(int j = 0; j < norm.normInstances.size(); j++) {
                                if(settings[settingsCounter]== GIVEUP && ps.qs.get(i+j)==-1) {
                                    totalWeight.addToEntry(i+j, giveUpWeight);
                                    continue;
                                }
                                int newState = product.getNextRabinState(ps.qs.get(i+j), crdraList.get(i+j), sp.s());
                                if(settings[settingsCounter + newState + 1] == BREAKONCE) {
                                    totalWeight.addToEntry(i+j, breakOnceWeight);
                                }
                            }
                            i += norm.normInstances.size() -1;
                            settingsCounter += norm.wnraSize;
                        } else {
                            if(settings[settingsCounter] == BREAKONCE) {
                                totalWeight.addToEntry(i, breakOnceWeight);
                            } else if(settings[settingsCounter] == GIVEUP) {
                                totalWeight.addToEntry(i, giveUpWeight);
                            }
                            settingsCounter++;
                        }
                    }

                    return totalWeight;
                }, (d1, d2) -> {
                    int result = comparator.compare(d1, d2);
                    if(result <= 0) return d1;
                    return d2;
                }));
        
        log.info("Constructed weighted Graph");
        
        WeightedGraphRVF rf = new WeightedGraphRVF(weightedGraph, hashingFactory);

        initialValues = new HashMap<>();
        bestPolicies = new HashMap<>();
        
        bestActions = new HashMap<>();

//        Determine optimal policies within the AMECs.
        accComponents.forEach(acc -> {

            log.debug("-----Working on next AMEC");
            log.debug("AMEC state size: " + acc.states.size());

            RestrictedMDP rmdp = new RestrictedMDP(product);
            rmdp.domain.getActionTypes().forEach(a -> ((RestrictedActionType)a)
                    .setSpecificActionRestriction(acc.actions));

            if(acc.states.isEmpty()) {
                return;
            }
            State accState = acc.states.stream().findAny().get().s();

            log.debug("Set action restriction");
            RVValueIteration est = new RVValueIteration(rmdp.domain, discount, hashingFactory, delta, maxiters,
                    rf, crdraList.size(), comparator);
            DPrint.toggleCode(est.getDebugCode(),false);
            log.debug("about to plan greedy policy for AMEC");
            GreedyQVectorPolicy gpolicy = est.planFromState(accState);
            log.debug("Planned greedy policy for AMEC");

            // Do action restriction, find AMECs of AMECs, and determine the set of states we can do greedy things on
            
            RestrictedMDP greedymdp = new RestrictedMDP(rmdp);
            
            greedymdp.domain.getActionTypes().forEach(a -> ((RestrictedActionType)a).setSpecificActionRestriction(acc
                    .states.stream().collect(Collectors.toMap(hs -> hs,
                            hs -> gpolicy
                                    .policyDistribution(hs.s())
                                    .stream()
                                    .filter(ap -> ap.pSelection > 0)
                                    .map(ap -> ap.ga)
                                    .collect(Collectors.toList())))));
            
            Set<EndComponent> ecgreedy = amecFinder.getMaximalEndComponents(greedymdp.domain,
                    acc.states, hashingFactory);
            Set<EndComponent> acgreedy = filterByAccepting(crdraList, ecgreedy);
            
            Set<HashableState> greedyStates = acgreedy.stream()
                    .flatMap(ec -> ec.states.stream())
                    .collect(Collectors.toSet());
            
            // All this just to make sure that we have restricted the AMEC actions as much as we need to.
            Map<HashableState, List<Action>> greedyAMECactions = acgreedy.stream().map(ec -> ec.actions)
                    .reduce(new HashMap<>(), (u,v) -> {
                Set<HashableState> commonKeys = v.keySet();
                commonKeys.retainAll(u.keySet());
                commonKeys.forEach(k -> {
                    Set<Action> gas = new HashSet<>(u.get(k));
                    gas.addAll(v.get(k));
                    u.remove(k);
                    v.put(k, new ArrayList<>(gas));
                    });
                v.putAll(u);
                return v;
            });
            RestrictedMDP mostRestricted = new RestrictedMDP(greedymdp);
            mostRestricted.domain.getActionTypes().forEach(a -> ((RestrictedActionType)a)
                    .setSpecificActionRestriction(greedyAMECactions));
            
            Policy restrictedPolicy = new RandomPolicyMod(mostRestricted.domain);
            
            Policy epsgreedy = new VectorEpsilonGreedy(est, 0.05);

            acc.states.forEach(hs -> {
                if(!initialValues.containsKey(hs) || comparator.compare(initialValues.get(hs), est.value(hs)) < 0) {
                    if(greedyStates.contains(hs)) {
                        bestPolicies.put(hs, restrictedPolicy);
                    } else {
                        bestPolicies.put(hs, epsgreedy);
                    }
                    initialValues.put(hs, est.value(hs));
                }
            });
            
        });
        
        log.debug("Completed AMEC value iteration");


        FromMapVectorVF vfInit = new FromMapVectorVF(initialValues, hashingFactory,
                rf.addToAll.mapSubtract(1.0).mapDivide(1.0-discount));


        Set<HashableState> gs = S.stream()
                .filter(hs -> initialValues.containsKey(hs))
                .collect(Collectors.toSet());

        log.debug("Captured the good states");
        log.debug("# good states: " + gs.size());

        noUpdate = new HashSet<>(S);

        noUpdate.removeAll(Helper.allStatesThatCanReach(product.domain,gs,S,hashingFactory));
        log.debug("NO update: " + noUpdate.size());

        log.debug("Captured the bad states");

//        Perform value iteration on the product MDP to find best actions in all other states.
        RVValueIteration est = new RVValueIteration(product.domain, discount, hashingFactory, delta, maxiters,
                rf, crdraList.size(), comparator, false, noUpdate);

        DPrint.toggleCode(est.getDebugCode(),false);
        log.debug("About to run VI on whole product MDP");

        est.setValueFunctionInitialization(vfInit);
        est.planFromState(tempS);
                
        stateValues = est;

        for(HashableState hs : S) {
            List<QValueVector> qs = stateValues.qValues(hs.s());
            RealVector maxQ = qs.stream().map(q -> q.q).max(comparator).get();
            bestActions.put(hs, qs.stream().filter(q -> comparator.compare(q.q, maxQ) == 0).map(q -> q.a)
                    .collect(Collectors.toSet()));
            if(noUpdate.contains(hs)) continue;
            log.debug("----NEXT STATE OPTIMAL ACTION");
            log.debug(hs.s());
            log.debug(bestActions.get(hs));
            log.debug(qs);
        }
        
        log.debug("Determined optimal actions for all states");
    }

    /**
     * Lifts some set of episodes from the original state space into the product state space.
     * @param rawEpisodes a list of {@link Episode} objects in the original MDP
     * @return the episodes lifted into the product space.
     * */
    public List<Episode> getProductSpaceEpisodes(List<Episode> rawEpisodes) {
        List<Episode> productSpaceEpisodes = new ArrayList<>();
        for(Episode ea : rawEpisodes) {
            List<State> pStateSequence = this.getProductSpaceStateSequence(ea.stateSequence);
            if(pStateSequence == null) {
                continue;
            }
            Episode pEpisode = new Episode(product.initialState);
            if(ea.numTimeSteps() == 0) {
                productSpaceEpisodes.add(pEpisode);
                continue;
            }
            pEpisode.transition(new SimpleAction(CRDRAProductMDP.SeeFirstStateActionType.ACTION_SEE_FIRST_STATE),
            pStateSequence.get(1),0.0);
            for(int i = 1; i < pStateSequence.size()-1; i++) {
                pEpisode.transition(ea.action(i-1),pStateSequence.get(i), 0.);
            }
            productSpaceEpisodes.add(pEpisode);
        }
        return productSpaceEpisodes;
    }

    /**
     * Lifts one sequence of states into the product space.
     * @param stateList a sequence of states in the original MDP
     * @return the corresponding product MDP states.
     */
    public List<State> getProductSpaceStateSequence(List<State> stateList) {
        State s = product.initialState;
        HashableState start = hashingFactory.hashState(s);
        Comparator comp = stateValues.getComparator();

        Map<HashableState, Pair<List<State>,RealVector>> possibleHistories = Collections.singletonMap(start,
                new Pair<>(Collections.singletonList(start.s()),new ArrayRealVector(stateValues.size())));
        for(int i = 0; i < stateList.size(); i++) {
            final Map<HashableState, Pair<List<State>,RealVector>> historiesRef = possibleHistories;
            final double factor = Math.pow(discount, i);
            final State curState = stateList.get(i);
            possibleHistories = weightedGraph.keySet().stream()
                .filter(p -> historiesRef
                        .containsKey(p.getLeft()))
                .filter(p -> hashingFactory.hashState(((WrapperState)p.getRight().s()).s).equals(
                        hashingFactory.hashState(curState)))
                .filter(p -> !noUpdate.contains(p.getRight()))
                .collect(Collectors.toMap(Pair::getRight, p -> {
                    List<State> curSeq = new ArrayList<>(historiesRef.get(p.getLeft()).getLeft());
                    curSeq.add(p.getRight().s());
                    return new Pair<>(curSeq, historiesRef.get(p.getLeft()).getRight().add(weightedGraph.get(p)
                            .mapMultiply(factor)));},
                            (d1, d2) -> comp.compare(d1.getRight(), d2.getRight()) > 0 ? d2 : d1
                        ));

        }
        if(possibleHistories.isEmpty()) {
            return null;
        }
        
        WeightedGraphRVF rf = new WeightedGraphRVF(weightedGraph, hashingFactory);

        final double factor = Math.pow(discount, stateList.size());
        // Best interpretation of the current state.
        return possibleHistories
                .entrySet()
                .stream()
                .map(e -> {


                RealVector d = stateValues.value(e.getKey()).add(rf.addToAll.mapMultiply(-1./(1.-discount)))
                        .mapMultiply(-factor).add(e.getValue().getRight());
                    return new Pair<>(e.getValue().getLeft(), d);
                })
                .min((Pair<List<State>, RealVector> t, Pair<List<State>, RealVector> t1) -> comp.compare(t.getRight(),
                        t1.getRight())).get().getLeft();
    }

    private HashableState getPermaskipState(HashableState orig) {
        Set<HashableState> S = Helper.getAllReachableStates(product.domain, product.initialState, hashingFactory);
        State toReturn = S.stream().filter(hs -> hashingFactory.hashState(((CRDRAProductState)hs.s()).s).equals(orig))
                .findAny().get().s().copy();
        ((CRDRAProductState)toReturn).qs = new ArrayList<>(((CRDRAProductState)product.initialState).qs);
        return hashingFactory.hashState(toReturn);
    }

    /**
    * Computes the violation cost accrued in a given episode, in vector form.
    * @param stateList the list of states in a given episode
    * @return The violation cost accrued during that episode, in vector form.
    * */
    public RealVector getViolationCostForEpisode(List<State> stateList) {

        State s = product.initialState;
        HashableState start = hashingFactory.hashState(s);
        if(this.rpValues == null) {
            this.getViolationCostForRandomPolicy();
        }


        Comparator comp = rpValues.getComparator();
        Map<HashableState, RealVector> possibleHistories = Collections.singletonMap(start,
                new ArrayRealVector(normInstances.size()));
        for(int i = 0; i < stateList.size(); i++) {
            final Map<HashableState, RealVector> historiesRef = possibleHistories;
            final double factor = Math.pow(discount, i);
            final State curState = stateList.get(i);
            possibleHistories = weightedGraph.keySet().stream()
                .filter(p -> historiesRef
                        .containsKey(p.getLeft()))
                .filter(p -> ((CRDRAProductState)p.getRight().s()).s
                        .equals(curState))
                .filter(p -> !noUpdate.contains(p.getRight()))
                .collect(Collectors.toMap(Pair::getRight, p -> historiesRef.get(p.getLeft())
                                .add(weightedGraph.get(p).mapMultiply(factor)),
                            (d1, d2) -> comp.compare(d1, d2) > 0 ? d2 : d1
                        ));
        }
        WeightedGraphRVF rf = new WeightedGraphRVF(weightedGraph, hashingFactory);

        if(possibleHistories.isEmpty()) {

            possibleHistories.put(getPermaskipState(hashingFactory.hashState(stateList.get(stateList.size()-1))),
                    new ArrayRealVector(normInstances.size(),
                            (1.0 - Math.pow(discount, stateList.size()))/(1.0 - discount)));
        }

        final double factor = Math.pow(discount, stateList.size());
        // Best interpretation of the current state.
        return possibleHistories
                .entrySet()
                .stream()
                .map(e -> {
                    RealVector d = rf.addToAll.mapMultiply(-1./(1.-discount)).add(rpValues.value(e.getKey()))
                            .mapMultiply(-factor).add(e.getValue());
                    return new Pair<>(e.getKey(), d);
                })
                .min((Pair<HashableState, RealVector> t, Pair<HashableState, RealVector> t1) ->
                        comp.compare(t.getRight(), t1.getRight())).get().getRight();
    }

    /**
    * Compute the expected violation cost assuming that the agent follows a given (product-space) policy.
     * @param policy a product-space policy
     * @return the expected violation cost for undertaking the given policy (in vector form).
    * */
    public RealVector getViolationCostForPolicy(Policy policy) {
        double delta = 0.001;
        int maxiters = 10000;


        WeightedGraphRVF rf = new WeightedGraphRVF(weightedGraph, hashingFactory);
        VectorPolicyEvaluation wnraEval = new VectorPolicyEvaluation(product.domain, discount, hashingFactory,
                delta, maxiters, riskParam, noUpdate, rf, stateValues.size(), stateValues.getComparator());

        wnraEval.evaluatePolicy((EnumerablePolicy)policy, product.initialState);


        return rf.addToAll.mapMultiply(1.0/(1.-discount)).subtract(wnraEval.value(product.initialState));

    }

    /**
     * Compute the expected violation cost assuming that the agent follows the random  policy.
     * @return the expected violation cost for undertaking the random policy (in vector form).
     * */
    public RealVector getViolationCostForRandomPolicy() {

        WeightedGraphRVF rf = new WeightedGraphRVF(weightedGraph, hashingFactory);

        if(this.rpValues == null) {
            double delta = 0.001;
            int maxiters = 10000;

            
            rpValues = new VectorPolicyEvaluation(product.domain, discount, hashingFactory, delta, maxiters,
                    riskParam, noUpdate,  rf, stateValues.size(), stateValues.getComparator());

            rpValues.evaluatePolicy(new RestrictedRandomPolicy(product.domain,new HashMap<>(),hashingFactory), product.initialState);

        }
        return rf.addToAll.mapMultiply(1.0/(1.-discount)).subtract(rpValues.value(product.initialState));

//
    }

    /**
     * The core action taken by the agent at every timestep: essentially re-evaluates the agent's history and then
     * restricts the agent's next action to the optimal one from the agent's current product state.
     * @param stateList The sequence of states seen thus far.
     * */
    void recomputeBestActions(List<State> stateList) {

        State s = product.initialState;
        HashableState start = hashingFactory.hashState(s);
        Comparator comp = stateValues.getComparator();
        Map<HashableState, RealVector> possibleHistories = Collections.singletonMap(start,
                new ArrayRealVector(stateValues.size()));
        for(int i = 0; i < stateList.size(); i++) {
            final Map<HashableState, RealVector> historiesRef = possibleHistories;
            final double factor = Math.pow(discount, i);
            final State curState = stateList.get(i);
            possibleHistories = weightedGraph.keySet().stream()
                .filter(p -> historiesRef
                        .containsKey(p.getLeft()))
                .filter(p -> hashingFactory.hashState(((WrapperState)p.getRight().s()).s)
                        .equals(hashingFactory.hashState(curState)))
                .filter(p -> !noUpdate.contains(p.getRight()))
                .collect(Collectors.toMap(Pair::getRight, p ->
                                weightedGraph.get(p).mapMultiply(factor).add(historiesRef.get(p.getLeft())),
                            (d1, d2) -> comp.compare(d1, d2) > 0 ? d2 : d1
                        ));
        }

        if(possibleHistories.isEmpty()) {
            possibleHistories.put(getPermaskipState(hashingFactory.hashState(stateList.get(stateList.size()-1))),
                    new ArrayRealVector(normInstances.size(), (1.0 - Math.pow(discount, stateList.size()))/
                            (1.0 - discount)));
        }
        final double factor = Math.pow(discount, stateList.size());
        HashableState curState = possibleHistories
                .entrySet()
                .stream()
                .map(e -> {
                    RealVector d = stateValues.value(e.getKey()).mapMultiply(factor).subtract(e.getValue());

                
                    return new Pair<>(e.getKey(), d);
                })
                .max((Pair<HashableState, RealVector> t, Pair<HashableState, RealVector> t1) -> comp.compare(t.getRight(), t1.getRight())).get().getLeft();
        Action bestAction;


        
        if(initialValues.containsKey(curState) && comp.compare(stateValues.value(curState), initialValues.get(curState)) <= 0){ //&& stateValues.value(curState) <= initialValues.get(curState)) {
            bestAction = bestPolicies.get(curState).action(curState.s());
            if(bestAction instanceof CRDRAAction) {
                bestAction = ((CRDRAAction) bestAction).action;
            }
            nextActions = Collections.singletonList(bestAction);
        } else {
            nextActions = new ArrayList<>(bestActions.get(curState));
        }
    }
}
