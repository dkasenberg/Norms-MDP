/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.FullModel;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.graph.PredecessorCache;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author dkasenberg
 */
public class Helper {
    
    public static <T> Set<List<T>> transposeList(List<Set<T>> orig) {
        if(orig.isEmpty()) return new HashSet<>(Collections.singleton(Collections.emptyList()));
        Set<List<T>> sub = transposeList(orig.subList(1, orig.size()));
        Set<List<T>> toReturn = new HashSet<>();
        orig.get(0).stream().forEach(i -> {
            sub.stream().forEach(l -> {
                List<T> newList = new ArrayList<>(l);
                newList.add(0, i);
                toReturn.add(newList);
            });
        });
        return toReturn;
    }
    
    public static List<Double> scalarProduct(double d, List<Double> v) {
        return IntStream.range(0, v.size()).mapToObj(i -> d*v.get(i)).collect(Collectors.toList());
    }
    
    public static List<Double> vectorSum(List<Double> v1, List<Double> v2) {
        return IntStream.range(0, v1.size()).mapToObj(i -> v1.get(i) + v2.get(i)).collect(Collectors.toList());
    }
    
    public static List<Action> getAllActions(SADomain d, State s) {
        return getAllActions(d.getActionTypes(), s);
    }

    public static List<Action> getAllActions(List<ActionType> actionTypes, State s) {
        return actionTypes.stream()
                .flatMap(e -> { return e
                        .allApplicableActions(s).stream(); })
                .collect(Collectors.toList());
    }
    
    public static Set<HashableState> getAllReachableStates(SADomain d, State s0, HashableStateFactory hf) {
        Set<HashableState> allStates = new HashSet<>();
        Queue<State> frontier = new LinkedList<>();
        allStates.add(hf.hashState(s0));
        frontier.add(s0);
        FullModel model = (FullModel)d.getModel();
        while(!frontier.isEmpty()) {
            State nextState = frontier.remove();
            List<Action> as = getAllActions(d, nextState);
            as.stream().forEach(a -> {
                model.transitions(nextState,a).stream()
                        .filter(tp -> tp.p > 0)
                        .map(tp -> tp.eo.op)
                        .forEach(s -> {
                            boolean added = allStates.add(hf.hashState(s));
                            if(added) frontier.add(s);
                        });
            });
        }
        return allStates;
    }

    public static <T> Set<List<T>> powerSet(Collection<T> originalSet) {
        Set<List<T>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new ArrayList<>());
            return sets;
        }
        List<T> list = new ArrayList<>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<>(list.subList(1, list.size())); 
        powerSet(rest).stream().forEach((set) -> {
            List<T> newSet = new ArrayList<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        });		
        return sets;
    }
    
    public static <K,V> HashMap<V,K> reverse(Map<K,V> map) {
        HashMap<V,K> rev = new HashMap<>();
        for(Map.Entry<K,V> entry : map.entrySet()) {
            rev.put(entry.getValue(), entry.getKey());
        }
        return rev;
    }

    public static Set<HashableState> allStatesThatCanReach(SADomain d, Set<HashableState> ends, Set<HashableState> all, HashableStateFactory hf) {
        Set<HashableState> allStates = new HashSet<>();
        Queue<HashableState> frontier = new LinkedList<>();
        allStates.addAll(ends);
        frontier.addAll(ends);

        PredecessorCache pc = new PredecessorCache(hf);
        pc.computePre(d, all);

        while(!frontier.isEmpty()) {
            HashableState nextState = frontier.remove();
            pc.pre(nextState, d, all).stream()
                    .map(p -> p.getLeft())
                    .filter(s -> !allStates.contains(s))
                    .forEach(s -> {
                        allStates.add(s);
                        frontier.add(s);
                    });
        }
        return allStates;
    }
}
