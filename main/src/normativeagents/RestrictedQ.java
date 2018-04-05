/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public class RestrictedQ extends QLearning {

    public RestrictedQ(SADomain domain, double gamma, HashableStateFactory hashingFactory, double qInit, double learningRate, int maxEpisodeSize) {
        super(domain, gamma, hashingFactory, qInit, learningRate, maxEpisodeSize);
    }
    
    public Policy getLearningPolicy() {
        return this.learningPolicy;
    }
    
    @Override
    public List<QValue> getQs(HashableState s) {
        QLearningStateNode node = getStateNode(s);
        List<Action> gas = this.applicableActions(s.s());
        for(Action ga : gas) {
            if(node.qEntry.stream().allMatch(qv -> !ga.equals(qv.a))) {
                node.addQValue(ga, qInitFunction.qValue(s.s(), ga));
            }
        }
        return node.qEntry.stream()
                .filter(qv -> gas.contains(qv.a))
                .collect(Collectors.toList());
    }
    
}
