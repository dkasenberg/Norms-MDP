package normativeagents.single.domains.vacuum.messdistribution;

import normativeagents.misc.Pair;
import normativeagents.single.domains.vacuum.mess.VacuumMessType;
import normativeagents.single.domains.vacuum.state.VacuumState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by dkasenberg on 9/5/17.
 */
public class SimpleMessDistribution implements VacuumMessDistribution {

    protected Random rand;

    protected List<Pair<Double,VacuumMessType>> dist;

    public SimpleMessDistribution(List<Pair<Double, VacuumMessType>> dist) {
        this.dist = new ArrayList<>(dist);
        this.rand = new Random();
    }

    @Override
    public VacuumMessType sample() {
        double r = rand.nextDouble();
        double sum = 0.;
        for(Pair<Double, VacuumMessType> transition : dist) {
            sum += transition.getLeft();
            if(r < sum) {

                return transition.getRight();
            }
        }
        throw new RuntimeException("Probabilities don't add up to 1.");
    }

    @Override
    public List<Pair<Double, VacuumMessType>> transitions() {
        return dist;
    }

    @Override
    public List<Pair<Double, VacuumMessType>> transitions(VacuumState s) {
        List<Pair<Double, VacuumMessType>> possibleMessesForState = dist.stream()
                .filter(p->!s.disallowedMessTypes.contains(p.getRight().typeName)).collect(Collectors.toList());
        double sum = possibleMessesForState.stream().mapToDouble(p->p.getLeft()).sum();
        return possibleMessesForState.stream().map(p->new Pair<Double, VacuumMessType>(p.getLeft()/sum,p.getRight()))
                .collect(Collectors.toList());
    }
}
