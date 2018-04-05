/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.norminference;

import burlap.behavior.singleagent.Episode;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableStateFactory;
import normativeagents.normconflictresolution.NormConflictResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Grammar;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.util.grammar.ContextFreeGrammar;
import rabinizer.formulas.Formula;
import rabinizer.formulas.FormulaBinary;
import rabinizer.formulas.FormulaUnary;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author dkasenberg
 */
public abstract class NormInference extends AbstractProblem {

    public ContextFreeGrammar ltlGrammar;
    public OOSADomain domain;
    public State initialState;
    public HashableStateFactory hashingFactory;
    public Collection<Episode> observedBehavior;
    public Map<String, Double> cachedObjX;
    public Map<String, Integer> cachedSizes;
    public PrintStream output;

    protected Log log = LogFactory.getLog(PolicyNormInference.class);

    public NormInference(OOSADomain d, State s, HashableStateFactory hf, Collection<Episode> observedBehavior) {
        this(d, s, hf, observedBehavior, d.propFunctions().stream().map(pf -> pf.getName()).collect(Collectors.toList()));
    }

    public NormInference(OOSADomain d, State s, HashableStateFactory hf, Collection<Episode> observedBehavior,
                                                Collection<String> props) {
        super(1, 2);
        this.domain = d;
        this.initialState = s;
        this.hashingFactory = hf;
        this.observedBehavior = observedBehavior;
        this.cachedObjX = new HashMap<>();
        this.cachedSizes = new HashMap<>();
        try {
            this.ltlGrammar = LTLContextFreeGrammar.get(props);
        } catch(Exception e) {
            System.err.println("Could not parse LTL");
        }
        Logger.getLogger(PolicyNormInference.class).setLevel(Level.INFO);
    }

    @Override
    public void evaluate(Solution solution) {
        int[] codon = ((Grammar)solution.getVariable(0)).toArray();
        double discount = 0.99;

        String normText = ltlGrammar.build(codon);

        if(normText == null) {
            solution.setObjective(0, observedBehavior.size() / (1.0 - discount));
            solution.setObjective(1, 1000);
            // TODO penalize the objective value
        } else {
            normText = "G(" + normText + ")";
            if(cachedObjX.containsKey(normText)) {
                solution.setObjective(0, cachedObjX.get(normText));
            }
            if(cachedSizes.containsKey(normText)) {
                solution.setObjective(1, cachedSizes.get(normText));
                if(cachedObjX.containsKey(normText)) {
                    return;
                }
            }
            NormConflictResolver ncr = new NormConflictResolver(normText, domain, initialState, hashingFactory, discount);
            ncr.initialize();

            double objX = computeObjX(ncr, observedBehavior);

            int objS = getNumNodesFromFormula(ncr.normInstances.get(0).formula);
            log.info(normText + "\t" + objX + "\t" + objS);
            solution.setObjective(0, objX);
            cachedObjX.put(normText, objX);
            solution.setObjective(1, (double)objS);
            cachedSizes.put(normText, objS);
        }
    }

    protected abstract double computeObjX(NormConflictResolver ncr, Collection<Episode> trajectories);

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(numberOfVariables, numberOfObjectives);
        solution.setVariable(0, new Grammar(10));
        return solution;
    }

    private int getNumNodesFromFormula(Formula formula) {
        if(formula instanceof FormulaBinary) {
            return 1 + getNumNodesFromFormula(((FormulaBinary)formula).left)
                    + getNumNodesFromFormula(((FormulaBinary)formula).right);
        } else if(formula instanceof FormulaUnary) {
            return 1 + getNumNodesFromFormula(((FormulaUnary)formula).operand);
        }
        return 1;

    }
}
