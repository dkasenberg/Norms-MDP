/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.graph;

import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.Helper;
import normativeagents.misc.Either;
import normativeagents.misc.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A class for computing the set of maximal {@link EndComponent}s of a given MDP.
 * @author dkasenberg
 */
public class EndComponentFinder {

    /**
     * Restricts the domain of a {@link Map} from states to actions.
     * @param orig A map from states to a set of applicable/permissible actions.
     * @param restriction A set of states
     * @return A map containing orig, with its domain restricted to states in restriction.
     * */
    protected static Map<HashableState, List<Action>> restrict(Map<HashableState, List<Action>> orig, Set<HashableState> restriction) {
        return restriction.stream().filter(
                k -> orig.containsKey(k))
                .collect(Collectors.toMap(k -> k,
                        k -> orig.get(k)));
    }
    
    private static Set<HashableState> post(FullStateModel fsm, HashableState hs, Action a, HashableStateFactory hf) {
        List<StateTransitionProb> tps = fsm.stateTransitions(hs.s(), a);
        return tps.stream()
                .filter(tp -> tp.p > 0)
                .map(tp -> hf.hashState(tp.s))
                .collect(Collectors.toSet());
    }

    public class AugmentedVertex<T> {
        GraphVertex<T> vertex;
        public int index = -1;
        int lowlink = -1;
        boolean onStack = false;
        
        AugmentedVertex(GraphVertex<T> vertex) {
            this.vertex = vertex;
        }
    }

    /** A helper method for finding strongly connected components.
     * */
    private void strongConnect(AugmentedVertex v, int index, List<AugmentedVertex> S, Map<GraphVertex, AugmentedVertex> augGraph, Set<Set<AugmentedVertex>> allComponents) {
        Set<AugmentedVertex> component = new HashSet<>();
        v.index = index;
        v.lowlink = index;
        index++;
        S.add(0,v);
        v.onStack = true;
        for(Object wp : v.vertex.successors) {
            AugmentedVertex w = augGraph.get(wp);
            if(w.index == -1) {
                strongConnect(w, index, S, augGraph, allComponents);
                v.lowlink = Math.min(w.lowlink, v.lowlink);
            } else if(w.onStack) {
                v.lowlink = Math.min(v.lowlink, w.index);
            }
        }
        
        if(v.lowlink == v.index) {
            AugmentedVertex w;
            do {
                w = S.get(0);
                S.remove(0);
                w.onStack = false;
                component.add(w);
            } while (!w.equals(v));
        }
        if(!component.isEmpty()) {
            allComponents.add(component);
        }
    }

    /**
     * Computes the set of strongly connected components of an MDP (given as a set of states and applicable actions in
     * those states, and a model, along with a HashableStateFactory for hashing states.
     *
     * @param T a set of states
     * @param A a mapping from states to the set of actions applicable in those states
     * @param model the model of the environment
     * @param hf a {@link HashableStateFactory}
     * @return The set of strongly connected components of the given MDP.
     * */
    private Set<Set<HashableState>> getStronglyConnectedComponents(Set<HashableState> T, Map<HashableState, List<Action>> A, FullStateModel model, HashableStateFactory hf) {
        int index = 0;
        Set<Set<HashableState>> allComponents = new HashSet<>();
        List<AugmentedVertex> S = new ArrayList<>();
        Set<GraphVertex<DigraphElement>> graph = getDigraphInducedBy(T,A,model,hf);
        Map<GraphVertex, AugmentedVertex> augGraph = new HashMap<>();
        graph.stream().forEach((v) -> {
            augGraph.put(v, new AugmentedVertex(v));
        });
        Set<Set<AugmentedVertex>> vertexComps = new HashSet<>();
        for(AugmentedVertex v : augGraph.values()) {
            if(v.index == -1) {
                strongConnect(v, index, S, augGraph, vertexComps);
            }
        }
        
        for(Set<AugmentedVertex> comp : vertexComps) {
            Set<HashableState> component = new HashSet<>();
            comp.stream()
                    .map(v1 -> (Either<HashableState, 
                            Pair<HashableState,Action>>)
                            (v1.vertex.contents))
                    .filter(either 
                            -> either.map(s -> true, pair -> false))
                    .forEach(either -> {
                        either.apply(hs -> component.add(hs), null);
                    });
            if(!component.isEmpty()) {
                allComponents.add(component);
            }
        }
        return allComponents;
    }

    /**
     * Just a helper class for digraphs (graphs with nodes of two types); in this case, the nodes can be either
     * states or state-action pairs.
     * */
    public static class DigraphElement extends Either<HashableState,Pair<HashableState,Action>> {

        public DigraphElement(Optional<HashableState> l, Optional<Pair<HashableState, Action>> r) {
            super(l, r);
        }
        
        public static DigraphElement create(HashableState s, Action ga) {
            return new DigraphElement(Optional.empty(), Optional.of(new Pair(s,ga)));
        }
        
        public static DigraphElement create(HashableState s) {
            return new DigraphElement(Optional.of(s), Optional.empty());
        }
    }

    /**
     * Converts a given MDP into a digraph (a graph with nodes of two types: state nodes, and state-action nodes).
     * */
    //    TODO optimize this: can run out of memory on large problems
    private Set<GraphVertex<DigraphElement>> getDigraphInducedBy(Set<HashableState> T, Map<HashableState, List<Action>> A, FullStateModel model, HashableStateFactory hf) {

        Map<HashableState, GraphVertex<DigraphElement>> stateVertices = T.stream().collect(Collectors.toMap(t->t,t->new GraphVertex<>(DigraphElement.create(t))));

        Set<GraphVertex<DigraphElement>> pairVertices = A.entrySet().stream().flatMap(entry->entry.getValue().stream()
                .map(a -> new GraphVertex<>(DigraphElement.create(entry.getKey(),a)))).collect(Collectors.toSet());

        pairVertices.stream().forEach(v->{
            HashableState hs = v.contents.right.get().getLeft();
            Action a = v.contents.right.get().getRight();
            v.addSuccessors(post(model,hs,a,hf).stream().map(sp->stateVertices.get(sp)).collect(Collectors.toSet()));
            stateVertices.get(hs).addSuccessor(v);
        });


        pairVertices.addAll(stateVertices.values());

        return pairVertices;
    }

    /** Computes the maximal end components of a given MDP; an implementation of an algorithm in Baier and Katoen 2008.
     * @param d a domain (contains info about the MDP dynamics)
     * @param S a set of (hashed) states (the state space of the MDP)
     * @param hf a {@link HashableStateFactory}
     * @return the set of maximal end components for this MDP.
     *  */
    public Set<EndComponent> getMaximalEndComponents(SADomain d, Set<HashableState> S, HashableStateFactory hf) {
        Map<HashableState, List<Action>> A = new HashMap<>();

        FullStateModel model = (FullStateModel)((FactoredModel)d.getModel()).getStateModel();

        PredecessorCache pc = new PredecessorCache(hf);
        pc.computePre(d, S);
                
        for(HashableState s : S) {
            A.put(s, Helper.getAllActions(d, s.s()));
        }
        
        Set<Set<HashableState>> MEC = new HashSet<>();
        Set<Set<HashableState>> MECnew = new HashSet<>();
        MECnew.add(new HashSet<>(S));
        while(!MEC.equals(MECnew)) {
            MEC = MECnew;
            MECnew = new HashSet<>();
            for(Set<HashableState> T : MEC) {
                Queue<HashableState> R = new LinkedList<>();
                Set<Set<HashableState>> sccs = getStronglyConnectedComponents(T, restrict(A, T),model,hf);
                for(Set<HashableState> scc : sccs) {
                    for(HashableState hs : scc) {
                        A.put(hs,
                                A.get(hs)
                                        .stream()
                                        .filter(a -> 
                                                scc.containsAll(post(model, hs, a, hf)))
                                        .collect(Collectors.toList()));
                        if(A.get(hs).isEmpty()) {
                            R.add(hs);
                        }
                    }
                }
                
                while(!R.isEmpty()) {
                    HashableState s = R.remove();
                    T.remove(s);
                    Set<Pair<HashableState, Action>> pres = pc.pre(s,d,T);
                    for(Pair<HashableState, Action> pair : pres) {
                        HashableState t = pair.getLeft();
                        Action beta = pair.getRight();
                        A.get(t).remove(beta);
                        if(A.get(t).isEmpty()) {
                            R.add(t);
                        }
                    }
                }
                
                for(Set<HashableState> scc : sccs) {
                    scc.retainAll(T);
                    if(!scc.isEmpty()) {
                        MECnew.add(scc);
                    }
                }
            }
        }
        
        Set<EndComponent> toReturn = new HashSet<>();
        for(Set<HashableState> T : MEC) {
            EndComponent ec = new EndComponent();
            ec.states = T;
            ec.actions = restrict(A, T);
            
            toReturn.add(ec);
        }
        
        return toReturn;
    }

    /** Computes the maximal end components of a given MDP; an implementation of an algorithm in Baier and Katoen 2008.
     * @param d a domain (contains info about the MDP dynamics)
     * @param s0 the initial state of the MDP (the state space is computed using the model
     * @param hf a {@link HashableStateFactory}
     * @return the set of maximal end components for this MDP.
     *  */
    public Set<EndComponent> getMaximalEndComponents(SADomain d, State s0, HashableStateFactory hf) {
        Set<HashableState> S = Helper.getAllReachableStates(d, s0, hf);
        return getMaximalEndComponents(d, S, hf);
    }
    
}
