/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rewardvector.comparator;

import normativeagents.rabin.CRDRA;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;
import java.util.List;

/**
 *
 * @author dkasenberg
 */
public class WeightedSumComparator implements Comparator<RealVector> {
    public RealVector weights;
    public double threshold;
    
    public WeightedSumComparator(List<CRDRA> wnras) {
        this(wnras, 0.01);
    }
    
    public WeightedSumComparator(List<CRDRA> wnras, double threshold) {
        this.weights = new ArrayRealVector(wnras.stream().mapToDouble(wnra -> wnra.weight).toArray());
        this.threshold = threshold;
    }

    public double getDifference(RealVector r1, RealVector r2) {
        return weights.dotProduct(r1.subtract(r2));
    }

    @Override
    public int compare(RealVector t, RealVector t1) {
        double weightedsumdiff = getDifference(t, t1);
        if(Math.abs(weightedsumdiff) < threshold) return 0;
//        if(weightedsumdiff == 0) return 0;
        if(weightedsumdiff > 0) return 1;
        return -1;
    }
}
