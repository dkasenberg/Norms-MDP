/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.actions;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;
import normativeagents.Helper;
import normativeagents.mdp.CRDRAProductMDP;
import normativeagents.mdp.state.CRDRAProductState;
import normativeagents.parsing.LTLNorm;
import normativeagents.rabin.CRDRA;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static normativeagents.actions.CRDRAAction.BREAKONCE;
import static normativeagents.actions.CRDRAAction.MAINTAIN;
import static normativeagents.mdp.CRDRAProductMDP.SeeFirstStateActionType.ACTION_SEE_FIRST_STATE;

/**
 * {@link ActionType} class corresponding to the actions available in the product MDP.  An action corresponds to an action in
 * the underlying MDP, along with a set of "norm settings" that determine which DRA transitions the agent will choose
 * to suspend.
 *
 * @author dkasenberg
 *
 */
public class CRDRAActionType extends WrapperActionType {

        /**
         * The set of conflict resolution DRAs  ({@link CRDRA}).
         */
        private List<CRDRA> crdras;

        /**
        * The product MDP.
        */
        protected CRDRAProductMDP mdp;

        /**
         * The set of norms.
         */
        protected List<LTLNorm> norms;

        private static Set<Integer> possibleVals = new HashSet<>(Arrays.asList(MAINTAIN, BREAKONCE));
        private Set<int[]> possibleSettings;

        // We need to take in the set of norms, and we need a mapping from each individual norm instance to either a 

        public CRDRAActionType(ActionType a, CRDRAProductMDP mdp, List<LTLNorm> norms, List<CRDRA> crdras) {
            super(a);
            this.mdp = mdp;
            this.crdras = crdras;
            this.norms = norms;
            int length = norms.stream().mapToInt(n -> n.normInstances.size()).sum();

            this.possibleSettings =getPossibleSettings(length);
        }


        /**
         * @param length the number of active norms.
         * @return The set of all possible combinations of settings for this action type.
         * */
        public static Set<int[]> getPossibleSettings(int length) {
            return Helper.transposeList(IntStream.range(0, length)
                    .mapToObj(i -> possibleVals)
                    .collect(Collectors.toList()))
                    .stream()
                    .map(l -> ArrayUtils.toPrimitive(l.toArray(new Integer[0])))
                    .collect(Collectors.toSet());
        }

        /**
         * @param strRep The string representation of an {@link Action}.
         * @return The {@link CRDRAAction} associated with a given string representation of an action.
         * */
        @Override
        public Action associatedAction(String strRep) {
            return new CRDRAAction(actionType.associatedAction(strRep), new int[crdras.size()]);
        }


        /**
         * @param s A {@link CRDRAProductState}
        * @return A list of all associated {@link CRDRAAction} objects applicable in the given state.
        * */
        @Override
        public List<Action> allApplicableActions(State s) {
            CRDRAProductState ps = (CRDRAProductState)s;
            if(ps.s instanceof NullState && !this.typeName().equals(ACTION_SEE_FIRST_STATE)) {
                return new ArrayList<>();
            }
            List<Action> as = actionType.allApplicableActions(ps.s);
            return as.stream()
                    .flatMap(a -> possibleSettings.stream()
                    .map(setting -> new CRDRAAction(a, setting)))
                    .collect(Collectors.toList());
        }

        /**
         * @param s A state in the product MDP (must be a member of the {@link CRDRAProductState} class).
         * @return The set of all (non-product) actions applicable in the given state.
         */

        public List<Action> allApplicableNonProductActions(State s) {
            CRDRAProductState ps = (CRDRAProductState)s;
            if(ps.s instanceof NullState && !this.typeName().equals(ACTION_SEE_FIRST_STATE)) {
                return new ArrayList<>();
            }
            return actionType.allApplicableActions(ps.s);
        }
    }
