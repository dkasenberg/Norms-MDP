package normativeagents.rewardvector;

import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;

/**
 * Created by dan on 5/16/17.
 */
public class VectorBellmanOperator implements VectorDPOperator{

    @Override
    public RealVector apply(RealVector[] qs, Comparator<RealVector> comparator) {
        RealVector mx = qs[0];
        for(int i = 1; i < qs.length; i++) {
            RealVector qi = qs[i];
            if(comparator.compare(qi, mx) > 0) {
                mx = qi;
            }
        }
        return mx;
    }
}