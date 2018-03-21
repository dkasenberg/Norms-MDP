package normativeagents.single.domains.vacuum.mess;

import normativeagents.single.domains.vacuum.GraphicsProperties;
import normativeagents.single.domains.vacuum.state.VacuumMess;
import normativeagents.single.domains.vacuum.state.VacuumState;

/**
 * Created by dkasenberg on 9/8/17.
 */
public class VacuumMessType {
    public GraphicsProperties graphicsProperties;
    public boolean oneTime;
    public String typeName;
    public int originalDirtiness;
    public int robotStepOnHazardLevel;
    public int robotVacuumHazardLevel;
    public int humanHazardLevel;

    public VacuumMessType() {

    }

    public VacuumMessType(String typeName, int originalDirtiness, int robotStepOnHazardLevel, int robotVacuumHazardLevel,
                          int humanHazardLevel, boolean oneTime) {
        this.typeName = typeName;
        this.originalDirtiness = originalDirtiness;
        this.robotStepOnHazardLevel = robotStepOnHazardLevel;
        this.robotVacuumHazardLevel = robotVacuumHazardLevel;
        this.humanHazardLevel = humanHazardLevel;
        this.oneTime = oneTime;
    }

    public VacuumMessType withGraphicsProperties(GraphicsProperties gp) {
        this.graphicsProperties = gp;
        return this;
    }

    public void tick(VacuumState s, VacuumMess mess) {
    }

    @Override
    public boolean equals(Object obj) {
        return this.typeName.equals(((VacuumMessType)obj).typeName);
    }

    @Override
    public int hashCode() {
        return this.typeName.hashCode();
    }
}
