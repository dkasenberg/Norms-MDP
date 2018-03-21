/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.single.domains.shopworld;

import burlap.mdp.auxiliary.DomainGenerator;
import burlap.mdp.auxiliary.common.SinglePFTF;
import burlap.mdp.core.Domain;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.FactoredModel;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.mdp.singleagent.oo.OOSADomain;
import normativeagents.actions.ObjectParameterizedActionType;
import normativeagents.single.domains.shopworld.state.ShopWorldAgent;
import normativeagents.single.domains.shopworld.state.ShopWorldState;
import normativeagents.single.domains.shopworld.state.ShopWorldTrinket;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dkasenberg
 */
public class ShopWorldDomain implements DomainGenerator {
    
    public static final String PFINSTOCK = "inStock";
    public static final String VAR_SIZE = "size";
    public static final String VAR_COST = "cost";
    public static final String VAR_MONEY = "agentMoney";
    public static final String SIZESMALL = "small";
    public static final String SIZEMEDIUM = "medium";
    public static final String SIZELARGE = "large";
    public static final String CLASS_TRINKET = "trinket";
    public static final String CLASS_AGENT = "agent";
    public static final String VAR_HELD = "held";
    public static final String VAR_HIDDEN = "hidden";
    public static final String VAR_BOUGHT = "bought";
    public static final String VAR_INSTOCK = "inStock";
    public static final String PFHELD = "held";
    public static final String PFHIDDEN = "hidden";
    public static final String PFBOUGHT = "bought";
    public static final String ACTION_PICKUP = "pickUp";
    public static final String ACTION_HIDE = "hide";
    public static final String ACTION_BUY = "buy";
    public static final String ACTION_PUTDOWN = "putDown";
    public static final String ACTION_LEAVE = "leaveStore";
    public static final String ACTION_NOOP = "noop";
    public static final String VAR_LEFT = "leftStore";
    public static final String VAR_CAUGHT = "caughtStealing";
    public static final String PFLEFT = "leftStore";
    public static final String PFCAUGHT = "caughtStealing";
    public static final String VAR_VALUE = "value";

    protected double hidingEffectiveness;
    protected double smallVisibility;
    protected double medVisibility;
    protected double largeVisibility;

    protected double theftPenalty;
    protected double defaultReward;

    public ShopWorldDomain(double hidingEffectiveness, double smallVisibility,
            double medVisibility, double largeVisibility, double theftPenalty, double defaultReward)  {
        this.hidingEffectiveness = hidingEffectiveness;
        this.smallVisibility = smallVisibility;
        this.medVisibility = medVisibility;
        this.largeVisibility = largeVisibility;
        this.theftPenalty = theftPenalty;
        this.defaultReward = defaultReward;
    }

    public ShopWorldDomain() {
        this(0.7, 0.3, 0.5, 0.7, -100, -0.1);
    }

    @Override
    public OOSADomain generateDomain() {
        OOSADomain domain = new OOSADomain();
        List <String> sizeList = new ArrayList<>();
        sizeList.add(SIZESMALL);
        sizeList.add(SIZEMEDIUM);
        sizeList.add(SIZELARGE);

        domain.addStateClass(CLASS_AGENT, ShopWorldAgent.class).addStateClass(CLASS_TRINKET, ShopWorldTrinket.class);

        domain.addActionType(new PickupAction(ACTION_PICKUP)).addActionType(new HideAction(ACTION_HIDE))
                .addActionType(new PutDownAction(ACTION_PUTDOWN)).addActionType(new BuyAction(ACTION_BUY))
                .addActionType(new LeaveAction(ACTION_LEAVE)).addActionType(new NoopAction(ACTION_NOOP));

        domain.addPropFunction(new LeftPF(PFLEFT));
        domain.addPropFunction(new CaughtPF(PFCAUGHT));
        domain.addPropFunction(new InStockPF(PFINSTOCK));
        domain.addPropFunction(new HeldPF(PFHELD));
        domain.addPropFunction(new HiddenPF(PFHIDDEN));
        domain.addPropFunction(new BoughtPF(PFBOUGHT));
        domain.addPropFunction(new ClassPF(CLASS_AGENT));
        domain.addPropFunction(new ClassPF(CLASS_TRINKET));


        PropositionalFunction left = domain.propFunction(PFLEFT);

        for(String size : sizeList){
            domain.addPropFunction(new SizePF(size));
        }

        TerminalFunction tf = new SinglePFTF(left);

        RewardFunction rf = new ShopWorldRF(tf, theftPenalty, defaultReward);

        ShopWorldModel smodel = new ShopWorldModel();
        FactoredModel model = new FactoredModel(smodel, rf , tf);
        domain.setModel(model);

        return domain;
    }

    public static ShopWorldTrinket addTrinket(OOSADomain d, ShopWorldState s, double cost,
            double value, ShopWorldTrinket.Size size) {
        ShopWorldTrinket trinket = new ShopWorldTrinket(size, cost, value, "trinket" + (s.numObjects() -1));
        s.addObject(trinket);
        return trinket;
    }

    public static ShopWorldState oneAgentNoTrinkets(Domain d, double startMoney) {
        return new ShopWorldState(startMoney);
    }

    public static void setAgent(ShopWorldState s, double money, boolean left, boolean caught) {
        ShopWorldAgent agent = s.touchAgent();
        agent.money = money;
        agent.leftStore = left;
        agent.caughtStealing = caught;
    }

    public static void setAgent(ShopWorldState s, double money) {
        setAgent(s, money, false, false);
    }

    public static void setTrinket(ShopWorldTrinket trinket, double cost,
            double value, ShopWorldTrinket.Size size,
            boolean inStock, boolean held, boolean hidden, boolean bought) {
        ShopWorldTrinket nTrinket = trinket.copy();
        nTrinket.cost = cost;
        nTrinket.value = value;
        nTrinket.size = size;
        nTrinket.inStock = inStock;
        nTrinket.held = held;
        nTrinket.hidden = hidden;
        nTrinket.bought = bought;
    }

    public static void setTrinket(ShopWorldTrinket o, double cost, double value,
            ShopWorldTrinket.Size size) {
        setTrinket(o, cost, value, size, true, false, false, false);
    }

    public static class LeftPF extends PropositionalFunction{

        public LeftPF(String name) {
                super(name, new String[]{CLASS_AGENT});
        }

        @Override
        public boolean isTrue(OOState st, String[] params) {
                ObjectInstance src = st.object(params[0]);
                return (Boolean)src.get(VAR_LEFT);
        }
    }


    public static class CaughtPF extends PropositionalFunction {

        public CaughtPF(String name) {
                super(name, new String[]{CLASS_AGENT});
        }

        @Override
        public boolean isTrue(OOState st, String[] params) {
                ObjectInstance src = st.object(params[0]);
                return (Boolean)src.get(VAR_CAUGHT);
        }
    }

    public static class ClassPF extends PropositionalFunction {
        public ClassPF(String name) {
            super(name, new String[]{name});
        }

        @Override
        public boolean isTrue(OOState s, String[] params) {
            ObjectInstance src = s.object(params[0]);
            return src.className().equals(name);
        }
    }

    public static class InStockPF extends PropositionalFunction{

        public InStockPF(String name) {
                super(name, new String[]{CLASS_TRINKET});
        }

        @Override
        public boolean isTrue(OOState st, String[] params) {
                ObjectInstance src = st.object(params[0]);
                return (Boolean)src.get(VAR_INSTOCK);
        }
    }

    public static class HeldPF extends PropositionalFunction{

        public HeldPF(String name) {
                super(name, new String[]{CLASS_TRINKET});
        }

        @Override
        public boolean isTrue(OOState st, String[] params) {
                ObjectInstance src = st.object(params[0]);
                return (Boolean)src.get(VAR_HELD);
        }
    }

    public static class HiddenPF extends PropositionalFunction{

        public HiddenPF(String name) {
                super(name, new String[]{CLASS_TRINKET});
        }

        @Override
        public boolean isTrue(OOState st, String[] params) {
                ObjectInstance src = st.object(params[0]);
                return (Boolean)src.get(VAR_HIDDEN);
        }
    }

    public static class SizePF extends PropositionalFunction{
        public SizePF(String name) {
                super(name, new String[]{CLASS_TRINKET});
        }

        @Override
        public boolean isTrue(OOState st, String[] params) {
                ObjectInstance src = st.object(params[0]);
                return src.get(VAR_SIZE) == ShopWorldTrinket.Size.valueOf(this.name.toUpperCase());
        }
    }

    public static class BoughtPF extends PropositionalFunction{

        public BoughtPF(String name) {
                super(name, new String[]{CLASS_TRINKET});
        }

        @Override
        public boolean isTrue(OOState st, String[] params) {
                ObjectInstance src = st.object(params[0]);
                return (Boolean)src.get(VAR_BOUGHT);
        }
    }

    public class NoopAction extends ObjectParameterizedActionType {
        public NoopAction(String name) {
            super(name, new String[]{});
        }

        @Override
        protected boolean applicableInState(State st, ObjectParameterizedAction a) {
            if(!(st instanceof OOState)) throw new RuntimeException("Can't use this action on non-ShopWorld state");
            OOState s = (OOState)st;
            ObjectInstance agent = s.object(CLASS_AGENT);
            return (Boolean)agent.get(VAR_LEFT);
        }

    }

    public class LeaveAction extends ObjectParameterizedActionType {
//        protected Random rand;

        private LeaveAction(String name) {
            super(name, new String[]{});
//            rand = new Random();
        }

        public boolean applicableInState(State s, ObjectParameterizedAction a) {
            if(!(s instanceof OOState)) throw new RuntimeException("Can't use this action on non-OO state");
            OOState st = (OOState)s;
            ObjectInstance agent = st.object(CLASS_AGENT);
            return !((Boolean)agent.get(VAR_LEFT));
        }
    }

    public class PickupAction extends ObjectParameterizedActionType {

        public PickupAction(String name) {
            super(name, new String[]{ CLASS_TRINKET });
        }

        @Override
        public boolean applicableInState(State st, ObjectParameterizedAction a) {
            String[] params = a.getObjectParameters();

            //block must be clear

            if(!(st instanceof OOState)) throw new RuntimeException("Can't use this action on non-OO state");
            OOState s = (OOState)st;

            ObjectInstance trinket = s.object(params[0]);

            ObjectInstance agent = s.object(CLASS_AGENT);

            return (!(Boolean)agent.get(VAR_LEFT)) && (Boolean)trinket.get(VAR_INSTOCK);
        }

    }

    public class HideAction extends ObjectParameterizedActionType {

        public HideAction(String name) {
            super(name, new String[]{ CLASS_TRINKET });
        }

        public boolean applicableInState(State st, ObjectParameterizedAction a){


            String [] params = a.getObjectParameters();

            if(!(st instanceof OOState)) throw new RuntimeException("Can't use this action on non-OO state");
            OOState s = (OOState)st;

            //block must be clear

            ObjectInstance trinket = s.object(params[0]);

            ObjectInstance agent = s.object(CLASS_AGENT);

            return (Boolean)trinket.get(VAR_HELD) && !((Boolean)trinket.get(VAR_HIDDEN))
                    && !((Boolean)agent.get(VAR_LEFT));
        }

    }


    public class BuyAction extends ObjectParameterizedActionType {

        public BuyAction(String name) {
            super(name, new String[]{ CLASS_TRINKET });
        }

        @Override
        public boolean applicableInState(State st, ObjectParameterizedAction a){

            String [] params = a.getObjectParameters();

            if(!(st instanceof OOState)) throw new RuntimeException("Can't use this action on non-OO state");
            OOState s = (OOState)st;

            ObjectInstance trinket = s.object(params[0]);

            ObjectInstance agent = s.object(CLASS_AGENT);

            return (Boolean)trinket.get(VAR_HELD) && !((Boolean)agent.get(VAR_LEFT)) && (Double)trinket.get(VAR_COST) <=
                    (Double)agent.get(VAR_MONEY);
        }

    }

    public class PutDownAction extends ObjectParameterizedActionType {

        public PutDownAction(String name) {
            super(name, new String[]{ CLASS_TRINKET });
        }

        @Override
        public boolean applicableInState(State st, ObjectParameterizedAction a){

            String [] params = a.getObjectParameters();
            if(!(st instanceof OOState)) throw new RuntimeException("Can't use this action on non-ShopWorld state");
            OOState s = (OOState)st;

            ObjectInstance trinket = s.object(params[0]);

            ObjectInstance agent = s.object(CLASS_AGENT);

            return !((Boolean)agent.get(VAR_LEFT)) && (Boolean)trinket.get(VAR_HELD);
        }
        
    }
    
//    public static void main(String[] args) {
//		ShopWorldDomain swdg = new ShopWorldDomain();
//		//gwdg.setProbSucceedTransitionDynamics(0.75);
//
//		Domain d = swdg.generateDomain();
//
//
//		State s = oneAgentNoTrinkets(d);
//                addTrinket(d, s, 10.0, 20.0, SIZESMALL);
//                TerminalExplorer exp = new TerminalExplorer(d, s);
//                exp.addActionShortHand("p", ACTION_PICKUP);
//                exp.addActionShortHand("h", ACTION_HIDE);
//                exp.addActionShortHand("b", ACTION_BUY);
//                exp.addActionShortHand("d", ACTION_PUTDOWN);
//                exp.addActionShortHand("l", ACTION_LEAVE);
//
//                exp.explore();
//	}
    
}
