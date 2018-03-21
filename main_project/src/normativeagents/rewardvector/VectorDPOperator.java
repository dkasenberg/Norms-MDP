package normativeagents.rewardvector;

import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;

/**
 * Created by dan on 5/16/17.
 */
public interface VectorDPOperator {
    RealVector apply(RealVector [] qs, Comparator<RealVector> comparator);
}
