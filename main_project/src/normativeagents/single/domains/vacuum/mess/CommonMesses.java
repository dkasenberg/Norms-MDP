package normativeagents.single.domains.vacuum.mess;

import normativeagents.single.domains.vacuum.GraphicsProperties;

import java.awt.*;

/**
 * Created by dkasenberg on 9/6/17.
 */
public class CommonMesses {

    public static VacuumMessType normalMess(String name, int originalDirtiness) {
        return new VacuumMessType("mess",originalDirtiness,0,0,0,false)
                .withGraphicsProperties(new GraphicsProperties(Color.orange.darker(),"main_project/resources/dirt.png"));
    }

    public static VacuumMessType waterSpill(String name, int originalDirtiness) {
        return new WaterMessType("water",originalDirtiness,0,2,0, true)
                .withGraphicsProperties(new GraphicsProperties(Color.blue, "main_project/resources/water.png"));
    }

    public static VacuumMessType shardsOfGlass(String name, int originalDirtiness) {
        return new VacuumMessType("shardsOfGlass", originalDirtiness, 0,5,2,true)
                .withGraphicsProperties(new GraphicsProperties(Color.red,"main_project/resources/broken_bottle.png"));
    }
}
