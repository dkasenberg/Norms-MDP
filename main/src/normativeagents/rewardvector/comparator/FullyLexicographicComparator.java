/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package normativeagents.rewardvector.comparator;

import org.apache.commons.math3.linear.RealVector;

import java.util.Comparator;

/**
 *
 * @author dkasenberg
 */
public class FullyLexicographicComparator implements Comparator {

    @Override
    public int compare(Object t, Object t1) {
        RealVector r1 = (RealVector)t;
        RealVector r2 = (RealVector)t1;

        for(int i = r1.getDimension()-1; i >= 0; i--) {
            if(r1.getEntry(i) > r2.getEntry(i)) return 1;
            else if(r1.getEntry(i) < r2.getEntry(i)) return -1;
        }
        return 0;
    }
    
}
