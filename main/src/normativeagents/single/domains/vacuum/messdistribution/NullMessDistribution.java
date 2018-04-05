package normativeagents.single.domains.vacuum.messdistribution;

import normativeagents.misc.Pair;
import normativeagents.single.domains.vacuum.mess.VacuumMessType;
import normativeagents.single.domains.vacuum.state.VacuumState;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dkasenberg on 8/3/17.
 */
public class NullMessDistribution implements VacuumMessDistribution {
    @Override
    public VacuumMessType sample() {
        return null;
    }

    @Override
    public List<Pair<Double, VacuumMessType>> transitions() {
        return Arrays.asList(new Pair<>(1.0,null));
    }

    @Override
    public List<Pair<Double, VacuumMessType>> transitions(VacuumState s) {
        return transitions();
    }
}
