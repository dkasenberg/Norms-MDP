package normativeagents.single.domains.shopworld.state;

import burlap.mdp.core.oo.state.ObjectInstance;

import java.util.Arrays;
import java.util.List;

import static normativeagents.single.domains.shopworld.ShopWorldDomain.*;

/**
 * Created by dan on 5/15/17.
 */
public class ShopWorldTrinket implements ObjectInstance {

    public boolean inStock;
    public boolean held;
    public boolean hidden;
    public boolean bought;
    public Size size;
    public String name;

    public double cost;
    public double value;

    public enum Size {
        SMALL, MEDIUM, LARGE
    }


    private final static List<Object> keys = Arrays.asList(VAR_SIZE, VAR_COST, VAR_VALUE, VAR_BOUGHT,
            VAR_HIDDEN, VAR_HELD, VAR_INSTOCK);

    public ShopWorldTrinket(Size size, double cost, double value) {
        this.size = size;
        this.cost = cost;
        this.value = value;
        this.bought = false;
        this.hidden = false;
        this.held = false;
        this.inStock = true;
        this.name = "trinket";
    }

    public ShopWorldTrinket(Size size, double cost, double value, String name) {
        this.size = size;
        this.cost = cost;
        this.value = value;
        this.bought = false;
        this.hidden = false;
        this.held = false;
        this.inStock = true;
        this.name =name;
    }

    protected ShopWorldTrinket(Size size, double cost, double value, String name, boolean inStock,
                               boolean held, boolean hidden, boolean bought) {
        this.size = size;
        this.cost = cost;
        this.value = value;
        this.bought = bought;
        this.hidden = hidden;
        this.held = held;
        this.inStock = inStock;
        this.name = name;
    }

    @Override
    public String className() {
        return CLASS_TRINKET;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ShopWorldTrinket copyWithName(String objectName) {
        return new ShopWorldTrinket(size, cost, value, objectName, inStock, held, hidden, bought);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }

    @Override
    public Object get(Object variableKey) {
        if(!(variableKey instanceof String)){
            throw new RuntimeException("ShopWorldTrinket variable key must be a string");
        }

        String key = (String)variableKey;
        if(key.equals(VAR_BOUGHT)){
            return bought;
        }
        else if(key.equals(VAR_INSTOCK)){
            return inStock;
        }
        else if(key.equals(VAR_HIDDEN)) {
            return hidden;
        }
        else if(key.equals(VAR_HELD)) {
            return held;
        }
        else if(key.equals(VAR_SIZE)) {
            return size;
        }
        else if(key.equals(VAR_COST)) {
            return cost;
        }
        else if(key.equals(VAR_VALUE)) {
            return value;
        }

        throw new RuntimeException("Unknown key " + key);
    }

    @Override
    public ShopWorldTrinket copy() {
        return new ShopWorldTrinket(size, cost, value, name, inStock, held, hidden, bought);
    }
}
