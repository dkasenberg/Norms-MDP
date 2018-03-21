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
public class UniformMessDistribution implements VacuumMessDistribution {

    protected List<VacuumMessType> possibleMesses;
    protected Random rand;

    public UniformMessDistribution(List<VacuumMessType> possibleMesses) {
        this.possibleMesses = new ArrayList<>(possibleMesses);
        rand = new Random();
    }

    public UniformMessDistribution(List<VacuumMessType> possibleMesses, long seed) {
        this.possibleMesses = new ArrayList<>(possibleMesses);
        rand = new Random(seed);
    }

    @Override
    public VacuumMessType sample() {
        int index = rand.nextInt(possibleMesses.size());
        return possibleMesses.get(index);
    }

    @Override
    public List<Pair<Double, VacuumMessType>> transitions() {
        return possibleMesses.stream().map(mess->new Pair<>(1./possibleMesses.size(),mess)).collect(Collectors.toList());
    }

    @Override
    public List<Pair<Double, VacuumMessType>> transitions(VacuumState s) {
        List<VacuumMessType> possibleMessesForState = possibleMesses.stream()
                .filter(mess->!s.disallowedMessTypes.contains(mess.typeName))
                .collect(Collectors.toList());
        return possibleMessesForState.stream().map(mess->new Pair<>(1./possibleMessesForState.size(),mess)).collect(Collectors.toList());
    }
}
