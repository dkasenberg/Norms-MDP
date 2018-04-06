package normativeagents.examples.norminference;

import burlap.behavior.functionapproximation.dense.SparseToDenseFeatures;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearning;
import burlap.behavior.singleagent.learnfromdemo.apprenticeship.ApprenticeshipLearningRequest;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.state.GridAgent;
import burlap.domain.singleagent.gridworld.state.GridLocation;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.auxiliary.common.ConstantStateGenerator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import normativeagents.Helper;
import normativeagents.RestrictedQ;
import normativeagents.mdp.WrappedRewardFunction;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.norminference.PolicyNormInference;
import normativeagents.single.domains.grid.RevisedGridWorldRewardFunction;
import normativeagents.statehashing.HashableWrapperStateFactory;
import normativeagents.StateUniqueFeatures;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.Grammar;
import org.moeaframework.util.distributed.DistributedProblem;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dkasenberg on 4/6/18.
 */
public class NormInferenceGridWorldExample {

    private static final Map<String, Character> actionChars;
    static {
        actionChars = new HashMap<>();
        actionChars.put("north",'>');
        actionChars.put("south",'<');
        actionChars.put("west",'^');
        actionChars.put("east",'v');
    }

    private void runExample() {


        long timestamp = System.nanoTime();

        PrintWriter pw = null;
        try {
            pw = new PrintWriter("main/resources/nonprincipled_gridworld_results/derp" + timestamp + ".txt");
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

//        Initialize a pretty-random GridWorld.
        Random rand = new Random();
        GridWorldDomain gwd = new GridWorldDomain(4,4);
        RevisedGridWorldRewardFunction rf = new RevisedGridWorldRewardFunction(4,4);
        gwd.setRf(rf);
//        gwd.setDeterministicTransitionDynamics();
        gwd.setProbSucceedTransitionDynamics(0.8);
        List<GridLocation> locations = new ArrayList<>();
        int numLocations = 0;
        for(int i= 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                double reward = rand.nextGaussian();
                rf.setReward(i,j,reward);
//                System.out.print(reward);

                double roll = rand.nextDouble();
//                System.out.print("(");
//                if(roll < THRESHOLD) {
//                    System.out.print(numLocations);
                locations.add(new GridLocation(i,j,"location" + numLocations++));
//                }
//                else {
//                    System.out.print("-");
//                }
//                System.out.print(")");
//                System.out.print("\t");
            }
            System.out.println();
        }

        pw.println(Arrays.deepToString(rf.getRewardMatrix()));

        GridWorldState s = new GridWorldState(new GridAgent(0,0),locations);
//        gwd.setRf(new NullRewardFunction());
        SADomain domain = gwd.generateDomain();

        HashableStateFactory hashingFactory = new HashableWrapperStateFactory();

        ValueIteration vi = new ValueIteration(domain,0.99,hashingFactory,0.01,1000);
        GreedyQPolicy policy = vi.planFromState(s);
        char[][] policyActions = new char[4][4];
        Set<HashableState> S = Helper.getAllReachableStates(domain,s,hashingFactory);
        for(HashableState hs : S) {
            int x = (Integer)hs.s().get(new OOVariableKey("agent","x"));
            int y = (Integer)hs.s().get(new OOVariableKey("agent","y"));
            Action a = policy.action(hs.s());
            policyActions[x][y] = actionChars.get(a.actionName());
        }
        pw.println(Arrays.deepToString(policyActions));
        pw.println(vi.value(s));


        Environment env = new SimulatedEnvironment(domain,s);

        List<Episode> learnedEpisodes = new ArrayList<>();

        int numEpisodes = 30;
        int maxSteps = 30;

        for(int j = 0;j < numEpisodes; j++) {
            Episode learnedEpisode = new Episode(s);
            for(int i = 0; i < maxSteps; i++) {
                GridWorldState sp = (GridWorldState)env.currentObservation();
                Action a = policy.action(env.currentObservation());

                System.out.print("(" + sp.agent.x + "," + sp.agent.y +"): ");
                pw.print("(" + sp.agent.x + ", " + sp.agent.y+"): ");
                System.out.print(a + "; ");
                pw.print(actionChars.get(a.actionName()) + " : ");
                env.executeAction(policy.action(env.currentObservation()));
                pw.print(env.lastReward() + "; ");
                learnedEpisode.transition(a,env.currentObservation(),env.lastReward());
            }
            System.out.println();
            pw.println();
            env.resetEnvironment();
            learnedEpisodes.add(learnedEpisode);
            System.out.println(learnedEpisode.discountedReturn(0.99));
        }

        pw.println("----- LEARNED BY IRL");

        SADomain irlDomain = domain;
        State irlState = s;

        ApprenticeshipLearningRequest request = new ApprenticeshipLearningRequest(irlDomain,
                new ValueIteration(irlDomain,0.99,hashingFactory,0.001,1000),
                new SparseToDenseFeatures(new StateUniqueFeatures(irlDomain,irlState,hashingFactory)),
                learnedEpisodes,
                new ConstantStateGenerator(irlState));
        Policy irlPolicy = ApprenticeshipLearning.getLearnedPolicy(request);

        List<Episode> learnedBehaviors = new ArrayList<>();

        Environment envForLearnedPolicy = new SimulatedEnvironment(irlDomain,irlState);
        for(int i =0; i < numEpisodes; i++) {
            Episode e = new Episode(irlState);
            for(int t= 0; t < maxSteps; t++) {
                State sp = envForLearnedPolicy.currentObservation();
                Action a = irlPolicy.action(envForLearnedPolicy.currentObservation());
                e.transition(envForLearnedPolicy.executeAction(a));
            }
            learnedBehaviors.add(e);
            envForLearnedPolicy.resetEnvironment();
        }

        for(Episode irlEpisode : learnedBehaviors) {
            System.out.println(irlEpisode.discountedReturn(0.99));
            printEpisode(irlEpisode,pw);
        }
        pw.flush();

        pw.println("-CONDUCTING NORM INFERENCE");

        gwd.setRf(new WrappedRewardFunction(gwd.getRf()));
        domain = gwd.generateDomain();

        ExecutorService exec = Executors.newSingleThreadExecutor();
//        ExecutorService exec = Executors.newFixedThreadPool(5);

        Collection<String> props = new ArrayList<>();
        //props.add("atLocation(x,y)");
        for(GridLocation loc : s.locations) {
            props.add("atLocation(x," + loc.getName() + ")");
        }
        props.add("wallToNorth(x)");
        props.add("wallToSouth(x)");
        props.add("wallToEast(x)");
        props.add("wallToWest(x)");

        PolicyNormInference inf = new PolicyNormInference((OOSADomain)domain, s, hashingFactory,
                learnedEpisodes, props);
        DistributedProblem problem = new DistributedProblem(inf, exec);
        long beginTime = System.nanoTime();
        NondominatedPopulation result= new Executor().withProblem(problem)
                .withAlgorithm("NSGAII")
                .withProperty("populationSize", 10)
                .withMaxEvaluations(100)
                .run();
        long duration = System.nanoTime() -beginTime;

        System.out.println(duration);

        exec.shutdown();

        for(Solution solution : result) {

            int[] codon = ((Grammar)solution.getVariable(0)).toArray();
            double discount = 0.99;

            String norm = inf.ltlGrammar.build(codon);
            if(solution.getObjective(1) != 0) {
                System.out.println(norm + "\t" + solution.getObjective(0) + "\t"
                        + solution.getObjective(1));
                pw.println(norm + "\t" + solution.getObjective(0) + "\t"
                        + solution.getObjective(1));
            }
        }

        for(Solution solution : result) {

            int[] codon = ((Grammar)solution.getVariable(0)).toArray();
            double discount = 0.99;

            String norm = "G(" + inf.ltlGrammar.build(codon) + ")";
            pw.println("----- FOR norm " + norm + ":");
            NormConflictResolver normSystem = new NormConflictResolver(norm,domain,s,hashingFactory,discount);
            normSystem.initialize();
            SADomain newD = normSystem.getCurrentDomain();
            Environment normEnv = normSystem.getEnvironment();


//            The problem is running this episode with the original reward function.
            RestrictedQ agent = new RestrictedQ(newD, discount, hashingFactory, 0.3, 0.0,maxSteps);

            for(int i = 0; i < numEpisodes; i++) {
                Episode niEpisode = agent.runLearningEpisode(normEnv, maxSteps);
                for(int t = 0; t < niEpisode.maxTimeStep(); t++) {
                    State sp = niEpisode.state(t);
                    Action a = niEpisode.action(t);
                    int x = (Integer)sp.get(new OOVariableKey("agent", "x"));
                    int y = (Integer)sp.get(new OOVariableKey("agent", "y"));
                    pw.print("(" + x + "," + y +"): ");
                    pw.print(actionChars.get(a.actionName()) + " : ");
                    pw.print(niEpisode.reward(t+1));
                    pw.print("; ");
                }
                normEnv.resetEnvironment();
                pw.println();
            }
        }

        pw.flush();
        pw.close();

    }

    private void printEpisode(Episode episode, PrintWriter pw) {
        for(int t = 0; t < episode.maxTimeStep(); t++) {
            State sp = episode.state(t);
            Action a = episode.action(t);
            int x = (Integer)sp.get(new OOVariableKey("agent", "x"));
            int y = (Integer)sp.get(new OOVariableKey("agent", "y"));
            pw.print("(" + x + "," + y +"): ");
            pw.print(actionChars.get(a.actionName()) + " : ");
            pw.print(episode.reward(t+1));
            pw.print("; ");
        }
        pw.println();
    }

    public static void main(String[] args) {
        NormInferenceGridWorldExample example = new NormInferenceGridWorldExample();
        for(int i = 0; i < 50; i++) {
            example.runExample();
        }
    }

}
