package normativeagents.mdp.state;

import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by dan on 5/16/17.
 */
public class CRDRAProductState extends WrapperState {
    public List<Integer> qs;
//    public List<CRDRA> crdras;

    public CRDRAProductState() {

    }

    public CRDRAProductState(State origState, List<Integer> rabinStates
//            , List<CRDRA> crdras
    ) {
        super(origState);
        this.qs = new ArrayList<>(rabinStates);
//        this.crdras = crdras;
    }

    public List<Object> uniqueKeys() {
        return IntStream.range(0, qs.size()).mapToObj(i -> i).collect(Collectors.toList());
    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey instanceof Integer) {
            int key = (int)variableKey;
            return qs.get(key);
        }
        return s.get(variableKey);
    }

    @Override
    public String toString() {
        String str = "ProductState(\n" + s.toString() + "\n,\n";
        for(int i = 0; i < qs.size(); i++) {
            if(i != 0) str = str + ",";
            str = str + qs.get(i);
        }
        return str + ")";
    }

    @Override
    public State copy() {
        return new CRDRAProductState(s.copy(), qs
//                , crdras
        );
    }

}
