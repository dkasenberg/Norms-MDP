package normativeagents.single.domains.vacuum;

import java.awt.*;

/**
 * Created by dkasenberg on 9/7/17.
 */
public class GraphicsProperties {
    public int colorR;
    public int colorG;
    public int colorB;
    public int colorA;
    public String imagePath;

    public GraphicsProperties(Color color, String imagePath) {
        this.colorR = color.getRed();
        this.colorG = color.getGreen();
        this.colorB = color.getBlue();
        this.colorA = color.getAlpha();

        this.imagePath = imagePath;
    }

    public GraphicsProperties() {
    }
}
