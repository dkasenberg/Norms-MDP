package normativeagents.single.domains.shopworld.state;

import burlap.mdp.core.oo.state.ObjectInstance;
import normativeagents.single.domains.shopworld.ShopWorldDomain;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.shopworld.ShopWorldDomain.*;

/**
 * Created by dan on 5/15/17.
 */
public class ShopWorldAgent implements ObjectInstance {

    public boolean leftStore;
    public boolean caughtStealing;
    public double money;

    protected String name;

    private final static List<Object> keys = Arrays.asList(VAR_MONEY, VAR_LEFT, VAR_CAUGHT);

    public ShopWorldAgent() {
        this.name = "agent";
        this.leftStore = false;
        this.caughtStealing = false;
        this.money = 100;
    }

    public ShopWorldAgent(double money) {
        this.name = "agent";
        this.leftStore = false;
        this.caughtStealing = false;
        this.money = money;
    }

    public ShopWorldAgent(double money, String name) {
        this.name = name;
        this.leftStore = false;
        this.caughtStealing = false;
        this.money = money;
    }

    protected ShopWorldAgent(double money, boolean left, boolean caught, String name) {
        this.name = name;
        this.leftStore = left;
        this.caughtStealing = caught;
        this.money = money;
    }



    @Override
    public String className() {
        return ShopWorldDomain.CLASS_AGENT;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ShopWorldAgent copyWithName(String objectName) {
        return new ShopWorldAgent(money, leftStore, caughtStealing, objectName);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("ShopWorldAgent variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(VAR_MONEY)){
            return money;
        }
        else if(key.equals(VAR_LEFT)){
            return leftStore;
        }
        else if(key.equals(VAR_CAUGHT)) {
            return caughtStealing;
        }

        throw new RuntimeException("Unknown key " + key);
    }

    @Override
    public ShopWorldAgent copy() {
        return new ShopWorldAgent(money, leftStore, caughtStealing, name);
    }
}
