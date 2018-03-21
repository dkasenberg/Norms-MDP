package normativeagents.single.domains.vacuum.messdistribution;

import normativeagents.misc.Pair;
import normativeagents.single.domains.vacuum.mess.VacuumMessType;
import normativeagents.single.domains.vacuum.state.VacuumState;

import java.util.List;

/**
 * Created by dkasenberg on 8/3/17.
 */
public interface VacuumMessDistribution {
    VacuumMessType sample();

    List<Pair<Double, VacuumMessType>> transitions();

    List<Pair<Double, VacuumMessType>> transitions(VacuumState s);
}
