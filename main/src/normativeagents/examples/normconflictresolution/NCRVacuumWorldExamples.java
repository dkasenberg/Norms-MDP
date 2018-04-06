package normativeagents.examples.normconflictresolution;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.singleagent.common.VisualActionObserver;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServer;
import burlap.mdp.singleagent.oo.OOSADomain;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.HashableStateFactory;
import burlap.visualizer.Visualizer;
import normativeagents.Helper;
import normativeagents.RestrictedQ;
import normativeagents.misc.Pair;
import normativeagents.normconflictresolution.NormConflictResolver;
import normativeagents.single.domains.vacuum.VacuumDomainGenerator;
import normativeagents.single.domains.vacuum.mess.CommonMesses;
import normativeagents.single.domains.vacuum.mess.VacuumMessType;
import normativeagents.single.domains.vacuum.messdistribution.SimpleMessDistribution;
import normativeagents.single.domains.vacuum.messdistribution.VacuumMessDistribution;
import normativeagents.single.domains.vacuum.state.*;
import normativeagents.single.domains.vacuum.visualization.VacuumVisualExplorer;
import normativeagents.single.domains.vacuum.visualization.VacuumVisualizer;
import normativeagents.statehashing.HashableWrapperStateFactory;

import java.util.*;

import static normativeagents.single.domains.vacuum.VacuumDomainGenerator.*;
import static normativeagents.single.domains.vacuum.VacuumDomainGenerator.ACTION_WARN;
import static normativeagents.single.domains.vacuum.state.VacuumState.CLASS_DOCKER;
import static normativeagents.single.domains.vacuum.state.VacuumState.CLASS_ROBOT;

/**
 * Created by dkasenberg on 4/6/18.
 */
public class NCRVacuumWorldExamples {

    protected VacuumState startStateScenario1() {
        VacuumRobot robot = new VacuumRobot(CLASS_ROBOT,10,10,10,
                10,0,0, false, false,false);
        VacuumDocker docker = new VacuumDocker(CLASS_DOCKER,3,0,0);
        VacuumHuman human = new VacuumHuman("homer",/*10,*/0.5,0.2,1,0,
                false,0.,0.,0.,
                0.,false,new HashSet<>(),false);
        return new VacuumState(robot,
                Collections.EMPTY_MAP,
                new HashMap<>(Collections.EMPTY_MAP),
                new HashMap<>(Collections.singletonMap(human.name(),human)),
                new HashMap<>(Collections.singletonMap(docker.name(), docker)));
    }

    protected VacuumState startStateScenario2() {
        VacuumRobot robot = new VacuumRobot(CLASS_ROBOT,10,10,10,
                10,0,0, false, false,false);
        VacuumDocker docker = new VacuumDocker(CLASS_DOCKER,3,0,0);
        VacuumHuman human = new VacuumHuman("homer",/*10,*/0.5,0.2,1,0,
                false,0.,0.,0.,
                0.,false,new HashSet<>(),false);
        VacuumMess mess = new VacuumMess("water", CommonMesses.waterSpill("water",3),0,0);

        VacuumState s = new VacuumState(robot,
                Collections.EMPTY_MAP,
                new HashMap<>(Collections.EMPTY_MAP),
                new HashMap<>(Collections.singletonMap(human.name(),human)),
                new HashMap<>(Collections.singletonMap(docker.name(), docker)));
        s.addMess(mess);
        return s;
    }

    protected VacuumState startStateScenario3() {
        VacuumRobot robot = new VacuumRobot(CLASS_ROBOT,10,10,10,
                10,0,0, false, false,false);
        VacuumDocker docker = new VacuumDocker(CLASS_DOCKER,3,0,0);
        VacuumHuman human = new VacuumHuman("homer",/*10,*/0.5,0.2,1,0,
                false,0.,0.,0.,
                0.,false,new HashSet<>(),false);
        VacuumMess mess = new VacuumMess("glass",CommonMesses.shardsOfGlass("glass",1),0,0);

        VacuumState s = new VacuumState(robot,
                Collections.EMPTY_MAP,
                new HashMap<>(Collections.EMPTY_MAP),
                new HashMap<>(Collections.singletonMap(human.name(),human)),
                new HashMap<>(Collections.singletonMap(docker.name(), docker)));
        s.addMess(mess);
        return s;
    }

    protected VacuumState startStateScenario4() {
        VacuumRobot robot = new VacuumRobot(CLASS_ROBOT,5,5,10,10,
                0,0, false, false,true);
        VacuumDocker docker = new VacuumDocker(CLASS_DOCKER,3,0,0);
        VacuumHuman human = new VacuumHuman("homer",/*10,*/0.5,0.,1,0,
                false,0.,0.,0.8,
                0,true,new HashSet<>(),false);
        VacuumMess mess = new VacuumMess("glass",CommonMesses.shardsOfGlass("glass",1),0,0);
        VacuumState s = new VacuumState(robot,
                Collections.EMPTY_MAP,
                new HashMap<>(Collections.EMPTY_MAP),
                new HashMap<>(Collections.singletonMap(human.name(),human)),
                new HashMap<>(Collections.singletonMap(docker.name(), docker)));
        s.addMess(mess);
        return s;
    }

    protected VacuumMessDistribution messDistributionScenario1() {
        List<Pair<Double, VacuumMessType>> messList = Arrays.asList(
                new Pair<>(1.0,CommonMesses.normalMess("normal",2))
        );
        return new SimpleMessDistribution(messList);
    }

    protected VacuumMessDistribution messDistributionVisualizer() {
        List<Pair<Double, VacuumMessType>> messList = Arrays.asList(
                new Pair<>(0.2,CommonMesses.normalMess("normal",2))
                , new Pair<>(0.8, CommonMesses.shardsOfGlass("shardsOfGlass",2))
        );
        return new SimpleMessDistribution(messList);
    }

    protected VacuumMessDistribution messDistributionScenario2() {
        List<Pair<Double, VacuumMessType>> messList = Arrays.asList(
                new Pair<>(1.,CommonMesses.normalMess("normal",2))
        );
        return new SimpleMessDistribution(messList);
    }

    protected VacuumMessDistribution messDistributionScenario3() {
        List<Pair<Double, VacuumMessType>> messList = Arrays.asList(
                new Pair<>(1.,CommonMesses.normalMess("normal",2))
        );
        return new SimpleMessDistribution(messList);
    }

    protected VacuumMessDistribution messDistributionScenario4() {
        List<Pair<Double, VacuumMessType>> messList = Arrays.asList(
                new Pair<>(1.,CommonMesses.normalMess("normal",1))
        );
        return new SimpleMessDistribution(messList);
    }

    protected void runExample1() {
        VacuumDomainGenerator dg = new VacuumDomainGenerator(2,1);
        dg.setMessDistribution(messDistributionScenario1());
        dg.setAvoidCollisions(false);
        OOSADomain d = dg.generateDomain();
        OOState s = startStateScenario1();

        HashableStateFactory hf = new HashableWrapperStateFactory();

        String formula = "G roomClean";

        System.out.println("About to start with norms");
        long startTime = System.nanoTime();
        NormConflictResolver ncr = new NormConflictResolver(formula, d, s, hf,0.99);
        ncr.initialize();
        long duration = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + duration);

        EnvironmentServer env = (EnvironmentServer)ncr.getEnvironment();

        LearningAgent agent = new RestrictedQ(ncr.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);

        Visualizer visualizer = VacuumVisualizer.getVisualizer(dg.getMap());
        VisualActionObserver exp = new VisualActionObserver(visualizer);

        exp.initGUI();
        env.addObservers(exp);
        agent.runLearningEpisode(env);

    }

    protected void runExample2() {
        VacuumDomainGenerator dg = new VacuumDomainGenerator(2,1);
        dg.setMessDistribution(messDistributionScenario2());
        dg.setAvoidCollisions(false);
        OOSADomain d = dg.generateDomain();
        OOState s = startStateScenario2();


        HashableStateFactory hf = new HashableWrapperStateFactory();

        String formula = "1.0: G roomClean; 5.0: G ! robotDamaged";

        System.out.println("About to start with norms");
        long startTime = System.nanoTime();
        NormConflictResolver ncr = new NormConflictResolver(formula, d, s, hf,0.99);
        ncr.initialize();
        long duration = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + duration);

        EnvironmentServer env = (EnvironmentServer)ncr.getEnvironment();

        LearningAgent agent = new RestrictedQ(ncr.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);

        Visualizer visualizer = VacuumVisualizer.getVisualizer(dg.getMap());
        VisualActionObserver exp = new VisualActionObserver(visualizer);

        exp.initGUI();
        env.addObservers(exp);
        agent.runLearningEpisode(env);

    }

    protected void runExample3() {
        VacuumDomainGenerator dg = new VacuumDomainGenerator(2,1);
        dg.setMessDistribution(messDistributionScenario3());
        dg.setAvoidCollisions(false);
        OOSADomain d = dg.generateDomain();
        OOState s = startStateScenario3();

        HashableStateFactory hf = new HashableWrapperStateFactory();

        String formula = "1.0: G roomClean; 5.0: G ! robotDamaged; 100: G ! injured(x)";

        System.out.println("About to start with norms");
        long startTime = System.nanoTime();
        NormConflictResolver ncr = new NormConflictResolver(formula, d, s, hf,0.99);
        ncr.initialize();
        long duration = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + duration);

        EnvironmentServer env = (EnvironmentServer)ncr.getEnvironment();

        LearningAgent agent = new RestrictedQ(ncr.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);

        Visualizer visualizer = VacuumVisualizer.getVisualizer(dg.getMap());
        VisualActionObserver exp = new VisualActionObserver(visualizer);

        exp.initGUI();
        env.addObservers(exp);
        agent.runLearningEpisode(env);

    }

    protected void runExample4() {
        VacuumDomainGenerator dg = new VacuumDomainGenerator(2,1);
        dg.setMessDistribution(messDistributionScenario4());
        dg.setAvoidCollisions(false);
        OOSADomain d = dg.generateDomain();
        OOState s = startStateScenario4();

        HashableStateFactory hf = new HashableWrapperStateFactory();

        System.out.println(Helper.getAllReachableStates(d,s,hf).size());

        String formula = "1.0: G roomClean; 200.0: G ! robotDamaged; 40000: G ! injured(x); 5.0: G ((! speak(x) ) U (! talking(x)))";

        System.out.println("About to start with norms");
        long startTime = System.nanoTime();
        NormConflictResolver ncr = new NormConflictResolver(formula, d, s, hf,0.99);
        ncr.initialize();
        long duration = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + duration);

        EnvironmentServer env = (EnvironmentServer)ncr.getEnvironment();

        LearningAgent agent = new RestrictedQ(ncr.getCurrentDomain(), 0.99, hf, 0.3, 0.1, 5);

        Visualizer visualizer = VacuumVisualizer.getVisualizer(dg.getMap());
        VisualActionObserver exp = new VisualActionObserver(visualizer);

        exp.initGUI();
        env.addObservers(exp);
        agent.runLearningEpisode(env);

    }

    protected void runVisualizer() {
        VacuumDomainGenerator dg = new VacuumDomainGenerator(2,1);
        dg.setMessDistribution(messDistributionVisualizer());
        dg.setAvoidCollisions(false);
        OOSADomain d = dg.generateDomain();
        OOState s = startStateScenario4();

        Visualizer v = VacuumVisualizer.getVisualizer(dg.getMap());
        VisualExplorer exp = new VacuumVisualExplorer(d, v, s);
        exp.addKeyAction("w", "north", "");
        exp.addKeyAction("s", "south", "");
        exp.addKeyAction("a", "west", "");
        exp.addKeyAction("d", "east", "");

        exp.addKeyAction("v",ACTION_VACUUM,"");
        exp.addKeyAction("m",ACTION_WAIT,"");
        exp.addKeyAction("p",ACTION_DOCK,"");
        exp.addKeyAction("u",ACTION_UNDOCK,"");
        exp.addKeyAction("k",ACTION_WARN,"homer mess0_0");
        exp.initGUI();

    }

    public static void main(String[] args) {
        NCRVacuumWorldExamples examples = new NCRVacuumWorldExamples();
        examples.runExample1();
        examples.runExample2();
        examples.runExample3();
        examples.runExample4();
//        examples.runVisualizer();
    }

}
