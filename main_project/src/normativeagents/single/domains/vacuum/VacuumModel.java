package normativeagents.single.domains.vacuum;

import burlap.debugtools.RandomFactory;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;
import normativeagents.misc.Pair;
import normativeagents.single.domains.vacuum.mess.VacuumMessType;
import normativeagents.single.domains.vacuum.messdistribution.VacuumMessDistribution;
import normativeagents.single.domains.vacuum.state.VacuumHuman;
import normativeagents.single.domains.vacuum.state.VacuumMess;
import normativeagents.single.domains.vacuum.state.VacuumRobot;
import normativeagents.single.domains.vacuum.state.VacuumState;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static normativeagents.single.domains.vacuum.VacuumDomainGenerator.*;
import static normativeagents.single.domains.vacuum.state.VacuumHuman.VAR_ON_PHONE;
import static normativeagents.single.domains.vacuum.state.VacuumState.VAR_X_POS;
import static normativeagents.single.domains.vacuum.state.VacuumState.VAR_Y_POS;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class VacuumModel implements FullStateModel {
    int[][] map;
    protected double[][] transitionDynamics;
    protected Random rand = RandomFactory.getMapped(0);
    protected VacuumMessDistribution messDistribution;
    protected boolean avoidCollisions;

// We can think about the dynamics in terms of a set of deterministic methods, with a set of probabilistic decision points.
// We design methods that do all of the individual deterministic things, and then other nondeterministic methods that
// get the dynamics.

//    Eventual aspects of the model:
//    - obstacles which can be collided with;
//    - breakable obstacles with value that can become worthless messes
//    - Hazard/injury distributions?

    public VacuumModel(int[][] map,
                       double[][] transitionDynamics,
                       VacuumMessDistribution perHumanDistribution,
                       boolean avoidCollisions) {
        this.map = map;
        this.transitionDynamics = transitionDynamics;
        this.messDistribution = perHumanDistribution;
        this.avoidCollisions = avoidCollisions;
    }

    public List<StateTransitionProb> stateTransitions(State s, Action a) {
        List<StateTransitionProb> toReturn = deterministicTransition(updateMesses(s));
        toReturn = mapThroughFunction(toReturn,a, this::manageRobotPreHumanTP);
        toReturn = mapThroughFunction(toReturn, this::manageHumansTP);

//        toReturn = mapThroughDeterministicFunction(toReturn,this::updateMesses);
//        List<StateTransitionProb> toReturn = manageHumansTP(s);
        toReturn = mapThroughFunction(toReturn, a, this::manageRobotPostHumanTP);

//        We may need to check for identical states and sum their probabilities.
        return toReturn;
    }

    public State sample(State s, Action a) {
        s = s.copy();

//        This saves a little bit of runtime over the usual sampleByEnumeration.
//        It would be better if we could formulate a more complete computation tree and
//        sample all the way through that.

        s = this.updateMesses(s);
        s = sampleThroughFunction(s, a, this::manageRobotPreHumanTP);
        s = this.manageHumans(s);
        s = sampleThroughFunction(s, a, this::manageRobotPostHumanTP);
//        s = this.updateMesses(s);
        return s;
    }

    public State updateMesses(State s) {
        VacuumState sp = (VacuumState)(s.copy());
        List<VacuumMess> messes = new ArrayList<>(sp.messes.values());
        for(VacuumMess mess : messes) {
            mess.type.tick(sp,mess);
        }
        return sp;
    }

    public List<StateTransitionProb> manageHumansTP(State s) {
        VacuumState vs = (VacuumState)s.copy();

        List<StateTransitionProb> toReturn = Collections.singletonList(new StateTransitionProb(vs,1.0));

        for(VacuumHuman human : vs.humans.values()) {
            toReturn = mapThroughDeterministicFunction(toReturn,human.name(),this::deleteHumanInjury);
            toReturn = mapThroughFunction(toReturn, human.name(), this::manageHumanTelephone);
            toReturn = mapThroughFunction(toReturn, human.name(), this::maybeMakeMessTP);
            toReturn = mapThroughFunction(toReturn, human.name(), this::maybeMoveHumanTP);
        }
        return toReturn;
    }

    public State manageHumans(State s) {
        State sp = s.copy();

        for(VacuumHuman human : ((VacuumState)s).humans.values()) {
            sp = deleteHumanInjury(sp,human.name());
            sp = sampleThroughFunction(sp, human.name(), this::maybeMakeMessTP);
            sp = sampleThroughFunction(sp, human.name(), this::maybeMoveHumanTP);
            sp = sampleThroughFunction(sp, human.name(), this::manageHumanTelephone);
        }
        return sp;
    }

    public List<StateTransitionProb> manageHumanTelephone(State state, String humanName) {
        if(StateUtilities.stringOrBoolean(state.get(new OOVariableKey(humanName, VAR_ON_PHONE)))) {
            double stayOnPhoneProb = ((VacuumHuman)((OOState)state).object(humanName)).stayOnPhoneProbability;
            MutableOOState hangUp = (MutableOOState)state.copy();
            hangUp.set(new OOVariableKey(humanName, VAR_ON_PHONE), false);
            return Arrays.asList(new StateTransitionProb(hangUp, 1.-stayOnPhoneProb),
                    new StateTransitionProb(state.copy(), stayOnPhoneProb));
        } else {
            double startPhoneCallProb = ((VacuumHuman)((OOState)state).object(humanName)).startPhoneCallProbability;
            if(startPhoneCallProb == 0.0) return deterministicTransition(state);
            MutableOOState onCall =(MutableOOState)state.copy();
            onCall.set(new OOVariableKey(humanName, VAR_ON_PHONE), true);
            return Arrays.asList(new StateTransitionProb(onCall, 1.-startPhoneCallProb),
                    new StateTransitionProb(state.copy(), startPhoneCallProb));
        }
    }

//    Makes a human uninjured (they can get re-injured later).
    public State deleteHumanInjury(State s, String humanName) {
        ((VacuumState)s).humans.get(humanName).injured = false;
        return s;
    }

    public List<StateTransitionProb> maybeMakeMessTP(State s, String humanName) {
        VacuumHuman human = ((VacuumState)s).humans.get(humanName);

        List<StateTransitionProb> toReturn = makeMessTP(s, human.x, human.y);

        final double messProb = human.onPhone ? human.onPhoneMessProbability : human.messProbability;

        toReturn.stream().forEach(tp -> tp.p *= messProb);
        toReturn.add(new StateTransitionProb(s.copy(), 1.-messProb));
        return toReturn;
    }

    public List<StateTransitionProb> makeMessTP(State s, int x, int y) {
        BiFunction<State, VacuumMessType, State> makeMessAtLocation = (o, o2) -> addMessAtLocation((State)o,(VacuumMessType)o2, x,y);
        return getDistribution(s, messDistribution.transitions((VacuumState)s), makeMessAtLocation);
    }

    public State addMessAtLocation(State s, VacuumMessType messType, int x, int y) {
        VacuumMess newMess = new VacuumMess("mess",messType,x,y);
        ((VacuumState)s).addObject(newMess);
        return s;
    }

    public List<StateTransitionProb> maybeMoveHumanTP(State s, String humanName) {
        VacuumHuman human = ((VacuumState)s).humans.get(humanName);
        List<StateTransitionProb> toReturn = moveHumanTP(s,humanName);
        double movementProb = human.onPhone ? human.onPhoneMovementProbability : human.movementProbability;

        toReturn.stream().forEach(tp -> tp.p *= movementProb);
        toReturn.add(new StateTransitionProb(s.copy(),1.-movementProb));
        return toReturn;
    }

    public List<StateTransitionProb> moveHumanTP(State s, String humanName) {
        BiFunction<State, Integer, State> moveHumanByDirectionIndex = (o, o2) -> {
            int[] dirs = VacuumDomainGenerator.movementDirectionFromIndex(o2);
            return moveHuman(o,humanName, dirs[0], dirs[1]);
        };
        List<Pair<Double, Integer>> dirProbs = Arrays.asList(
                new Pair<>(0.25, 0),
                new Pair<>(0.25, 1),
                new Pair<>(0.25, 2),
                new Pair<>(0.25, 3)
        );
        return getDistribution(s, dirProbs, moveHumanByDirectionIndex);
    }

    //  Moves a human to a specified point, and does all the things relating to that.
    public State moveHuman(State s, String humanName, int xd, int yd) {
        VacuumState vs = (VacuumState)s;
        VacuumHuman human = vs.humans.get(humanName);
        int hx = human.x;
        int hy = human.y;
        int nx = hx + xd;
        int ny = hy + yd;

//        TODO also handle collision with robot
        if(collidesWithWall(hx, hy, nx, ny) || (avoidCollisions && (collidesWithHuman(vs, nx, ny) || collidesWithObstacle(vs, nx, ny) ||
                collidesWithRobot(vs, nx, ny)))) {
            return vs;
        }
        human.x = nx;
        human.y = ny;

//                handle human hazards
        vs.messes.values().stream().filter(mess -> mess.x == human.x && mess.y == human.y).forEach(mess -> {
            if(mess.type.humanHazardLevel > 0 && !human.knownMesses.contains(mess.name())) {
                human.injured = true;
            }
//            human.healthLevel = Math.max(0,human.healthLevel - mess.type.humanHazardLevel);
        });

        return vs;
    }

    public List<StateTransitionProb> manageRobotPreHumanTP(State s, Action a) {
        State sp = s.copy();
        ((VacuumState)sp).humans.values().forEach(human->human.justAddressedByRobot = false);
        if(a.actionName().equals(ACTION_WARN)) {
            String[] params = ((ObjectParameterizedAction)a).getObjectParameters();
            VacuumHuman human = ((VacuumState)sp).humans.get(params[0]);
            human.knownMesses.add(params[1]);
            human.justAddressedByRobot = true;
            return deterministicTransition(sp);
        }
        return deterministicTransition(sp);
    }

    public List<StateTransitionProb> manageRobotPostHumanTP(State s, Action a) {
        State sp = s.copy();
        sp = deleteRobotDamage(sp);
        if(a.actionName().equals(ACTION_WARN)) {
//            WARN is handled *before* the person moves or gets off the phone.
            return deterministicTransition(sp);
        }
        if(a.actionName().equals(ACTION_WAIT)) {
            return deterministicTransition(wait(sp));
        } else if(a.actionName().equals(ACTION_VACUUM)) {
            return deterministicTransition(vacuum(sp));
        } else if(a.actionName().equals(ACTION_BEDEAD)) {
            return deterministicTransition(sp);
        } else if(a.actionName().equals(ACTION_DOCK)) {
            return deterministicTransition(dock(sp));
        } else if(a.actionName().equals(ACTION_UNDOCK)) {
            return deterministicTransition(undock(sp));
        }
        return moveRobotTP(s,a);
    }

    public State deleteRobotDamage(State s) {
        ((VacuumState)s).robot.damaged = false;
        return s;
    }

    public State dock(State s) {
        VacuumState sp = (VacuumState)s;
        sp.robot.docked = true;
//        Run battery while docking?
        return sp;
    }

    public State undock(State s) {
        VacuumState sp = (VacuumState)s;
        sp.robot.docked = false;
        return sp;
    }

    public List<StateTransitionProb> deterministicTransition(State s) {
        return Collections.singletonList(new StateTransitionProb(s,1.0));
    }

    public List<StateTransitionProb> moveRobotTP(State s, Action a) {
        BiFunction<State, Integer, State> moveRobotByDirectionIndex = (o, o2) -> {
            int[] dirs = VacuumDomainGenerator.movementDirectionFromIndex(o2);
            return moveRobot(o, dirs[0], dirs[1]);
        };
        double[] dirProbs = this.transitionDynamics[this.actionInd(a.actionName())];
        List<Pair<Double, Integer>> decisionProbs= Arrays.asList(
                new Pair<>(dirProbs[0],0),
                new Pair<>(dirProbs[1],1),
                new Pair<>(dirProbs[2], 2),
                new Pair<>(dirProbs[3], 3)
        );

        return getDistribution(s, decisionProbs, moveRobotByDirectionIndex);
    }

    public State moveRobot(State s, int xd, int yd) {
        VacuumState sp = (VacuumState)s;

        VacuumRobot robot = sp.robot;

        int ax = robot.x;
        int ay = robot.y;
        int nx = ax + xd;
        int ny = ay + yd;

        if(collidesWithWall(ax, ay, nx, ny) || (avoidCollisions && (collidesWithHuman(sp, nx, ny) || collidesWithObstacle(sp, nx, ny)))) {
            nx = ax;
            ny = ay;
        }

//        handle collisions and hazards etc

        sp.robot.x = nx;
        sp.robot.y = ny;
        sp.robot.batteryLevel -= 1;

        sp.messes.values().stream().filter(mess->mess.x == robot.x && mess.y == robot.y).forEach(mess->{
            robot.healthLevel -= mess.type.robotStepOnHazardLevel;
            if(mess.type.robotStepOnHazardLevel > 0) {
                robot.damaged = true;
            }
        });

        return sp;
    }

    protected State wait(State s) {
        VacuumState sp = (VacuumState)s;
        if(sp.robot.docked) {
            sp.dockers.values().stream().filter(docker ->
                    docker.get(VAR_X_POS).equals(sp.robot.get(VAR_X_POS)) &&
                            docker.get(VAR_Y_POS).equals(sp.robot.get(VAR_Y_POS))).forEach(docker -> {
                 sp.robot.batteryLevel = Math.min(sp.robot.batteryLevel + docker.dockingSpeed, sp.robot.maxBatteryLevel);
            });
        } else {
            sp.robot.batteryLevel -= 1;
        }
        return sp;
    }

    protected State vacuum(State s) {
        VacuumState sp = (VacuumState)s;

//        handle hazards etc
        sp.robot.batteryLevel -= 2;
        if(sp.robot.batteryLevel < 0) return sp;

        int x = sp.robot.x;
        int y = sp.robot.y;

        VacuumMess mess = sp.getMessAt(x,y);
        if(mess != null) {
            mess.currentDirtiness -= 1;
            if(mess.currentDirtiness <= 0) {
                sp.removeObject(mess.name());
            }
            sp.robot.healthLevel -= mess.type.robotVacuumHazardLevel;
            if(mess.type.robotVacuumHazardLevel > 0) {
                sp.robot.damaged = true;
            }
        }

        return sp;
    }

    protected boolean collidesWithWall(int ax, int ay, int nx, int ny) {
        int xd = nx - ax;
        int yd = ny - ay;
        return nx < 0 || nx >= this.map.length || ny < 0 || ny >= this.map[0].length || this.map[nx][ny] == 1 ||
                xd > 0 && (this.map[ax][ay] == 3 || this.map[ax][ay] == 4) ||
                xd < 0 && (this.map[nx][ny] == 3 || this.map[nx][ny] == 4) ||
                yd > 0 && (this.map[ax][ay] == 2 || this.map[ax][ay] == 4) ||
                yd < 0 && (this.map[nx][ny] == 2 || this.map[nx][ny] == 4);
    }

    protected boolean collidesWithObstacle(VacuumState sp, int nx, int ny) {
        return sp.obstacles.values().stream().anyMatch(obst -> obst.x == nx && obst.y == ny && obst.solid);
    }

    protected boolean collidesWithHuman(VacuumState sp, int nx, int ny) {
        return sp.humans.values().stream().anyMatch(obst -> obst.x == nx && obst.y == ny);
    }

    protected boolean collidesWithRobot(VacuumState sp, int nx, int ny) {
        return sp.robot.x == nx && sp.robot.y == ny;
    }

//    Given a probability distribution over parameters, this gives a probability distribution over resulting states
    public <T> List<StateTransitionProb> getDistribution(State s,
                                                         List<Pair<Double, T>> decisionProbs,
                                                         BiFunction<State,T,State> deterministicFunction) {
        return decisionProbs.stream()
                .map(p -> new StateTransitionProb(deterministicFunction
                        .apply(s.copy(),p.getRight()),p.getLeft())).collect(Collectors.toList());
    }

//    Assuming we have a function mapping a state to TPs, this maps a whole set of STPs through that function.

    public List<StateTransitionProb> mapThroughDeterministicFunction(List<StateTransitionProb> origTransitions,
                                                                     Function<State, State> deterministicFunction) {
        return origTransitions.stream().map(tp -> new StateTransitionProb(deterministicFunction.apply(tp.s),tp.p))
                .collect(Collectors.toList());
    }

    public <T> List<StateTransitionProb> mapThroughDeterministicFunction(List<StateTransitionProb> origTransitions, T t,
                                                                     BiFunction<State, T, State> deterministicFunction) {
        return origTransitions.stream().map(tp -> new StateTransitionProb(deterministicFunction.apply(tp.s, t),tp.p))
                .collect(Collectors.toList());
    }



    public  List<StateTransitionProb> mapThroughFunction(List<StateTransitionProb> origTransitions,
                                                        Function<State, List<StateTransitionProb>> randomFunction) {
        return origTransitions
                .stream()
                .flatMap(p -> randomFunction.apply(p.s).stream().map(tp -> new StateTransitionProb(tp.s, tp.p*p.p)))
                .filter(tp->tp.p > 0)
                .collect(Collectors.toList());
    }

    public <T> List<StateTransitionProb> mapThroughFunction(List<StateTransitionProb> origTransitions, T t,
                                                         BiFunction<State, T, List<StateTransitionProb>> randomFunction) {
        return origTransitions
                .stream()
                .flatMap(p -> randomFunction.apply(p.s, t).stream().map(tp -> new StateTransitionProb(tp.s, tp.p*p.p)))
                .filter(tp->tp.p > 0)
                .collect(Collectors.toList());
    }



    public <T> State sampleThroughFunction(State s, T t, BiFunction<State, T, List<StateTransitionProb>> randomFunction) {
        double roll = rand.nextDouble();
        double curSum = 0.0D;
        List<StateTransitionProb> tp = randomFunction.apply(s,t);
        for(int i = 0; i < tp.size(); ++i) {
            curSum += tp.get(i).p;
            if(roll < curSum) {
                return tp.get(i).s;
            }
        }
        throw new RuntimeException("Transition probabilities don't sum to 1");
    }

    protected int actionInd(String name) {
        if(name.equals("north")) {
            return 0;
        } else if(name.equals("south")) {
            return 1;
        } else if(name.equals("east")) {
            return 2;
        } else if(name.equals("west")) {
            return 3;
        } else {
            throw new RuntimeException("Unknown action " + name);
        }
    }
}
