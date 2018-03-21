package normativeagents.single.domains.vacuum.mess;

import normativeagents.single.domains.vacuum.state.VacuumMess;
import normativeagents.single.domains.vacuum.state.VacuumState;

/**
 * Created by dkasenberg on 9/10/17.
 */
public class WaterMessType extends VacuumMessType {

    public WaterMessType() {
        super();
    }

    public WaterMessType(String typeName, int originalDirtiness, int robotStepOnHazardLevel, int robotVacuumHazardLevel, int humanHazardLevel, boolean oneTime) {
        super(typeName, originalDirtiness, robotStepOnHazardLevel, robotVacuumHazardLevel, humanHazardLevel, oneTime);
    }

    @Override
    public void tick(VacuumState s, VacuumMess mess) {
        mess.currentDirtiness = Math.max(0,mess.currentDirtiness-1);
        if(mess.currentDirtiness <= 0) {
            s.removeObject(mess.name);
        }
    }
}
