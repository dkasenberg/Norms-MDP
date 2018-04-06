//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package normativeagents.single.domains.vacuum.visualization;

import burlap.mdp.core.Domain;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.visualizer.*;
import normativeagents.single.domains.vacuum.GraphicsProperties;
import normativeagents.single.domains.vacuum.state.VacuumMess;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static normativeagents.single.domains.vacuum.state.VacuumHuman.VAR_ON_PHONE;
import static normativeagents.single.domains.vacuum.state.VacuumMess.VAR_CURR_DIRT;
import static normativeagents.single.domains.vacuum.state.VacuumRobot.*;
import static normativeagents.single.domains.vacuum.state.VacuumState.*;

public class VacuumVisualizer {
    private VacuumVisualizer() {
    }

    /** @deprecated */
    @Deprecated
    public static Visualizer getVisualizer(Domain d, int[][] map) {
        StateRenderLayer r = getRenderLayer(d, map);
        Visualizer v = new Visualizer(r);
        return v;
    }

    public static Visualizer getVisualizer(int[][] map) {
        StateRenderLayer r = getRenderLayer(map);
        Visualizer v = new Visualizer(r);
        return v;
    }

    /** @deprecated */
    @Deprecated
    public static StateRenderLayer getRenderLayer(Domain d, int[][] map) {
        StateRenderLayer r = new StateRenderLayer();
        r.addStatePainter(new VacuumVisualizer.MapPainter(map));
        OOStatePainter oopainter = new OOStatePainter();
        oopainter.addObjectClassPainter(CLASS_DOCKER, new VacuumVisualizer.DockerPainter(map));
        oopainter.addObjectClassPainter(CLASS_MESS, new VacuumVisualizer.MessPainter(map));
        oopainter.addObjectClassPainter(CLASS_HUMAN, new VacuumVisualizer.HumanPainter(map));
//        oopainter.addObjectClassPainter(CLASS_HUMAN,new VacuumVisualizer.CellPainter(1,Color.cyan,map));
        oopainter.addObjectClassPainter(CLASS_ROBOT,new VacuumVisualizer.RobotPainter(map));
        r.addStatePainter(oopainter);
        return r;
    }

    public static StateRenderLayer getRenderLayer(int[][] map) {
        StateRenderLayer r = new StateRenderLayer();
        r.addStatePainter(new VacuumVisualizer.MapPainter(map));
        OOStatePainter oopainter = new OOStatePainter();
        oopainter.addObjectClassPainter(CLASS_DOCKER, new VacuumVisualizer.DockerPainter(map));
        oopainter.addObjectClassPainter(CLASS_MESS, new VacuumVisualizer.MessPainter(map));
        oopainter.addObjectClassPainter(CLASS_HUMAN, new VacuumVisualizer.HumanPainter(map));
//        oopainter.addObjectClassPainter(CLASS_HUMAN,new VacuumVisualizer.CellPainter(1,Color.cyan,map));
        oopainter.addObjectClassPainter(CLASS_ROBOT,new VacuumVisualizer.RobotPainter(map));
        r.addStatePainter(oopainter);
        return r;
    }

    public static class HumanPainter implements ObjectPainter {
        protected int dwidth;
        protected int dheight;
        protected int[][] map;
        protected BufferedImage img;
        protected BufferedImage phoneImg;

        public HumanPainter(int[][] map) {
            this.dwidth = map.length;
            this.dheight = map[0].length;
            this.map = map;
            try {
                File f = new File("main/resources/stick_figure.png");

                this.img = ImageIO.read(f);
            } catch(IOException exception) {
                exception.printStackTrace();
                this.img = null;
            }
            try {
                File f = new File("main/resources/stick_figure_phone.png");

                this.phoneImg = ImageIO.read(f);
            } catch(IOException exception) {
                exception.printStackTrace();
                this.phoneImg = null;
            }

        }

        @Override
        public void paintObject(Graphics2D g2, OOState ooState, ObjectInstance objectInstance, float cWidth, float cHeight) {
            float domainXScale = (float)this.dwidth;
            float domainYScale = (float)this.dheight;
            float width = 1.0F / domainXScale * cWidth;
            float height = 1.0F / domainYScale * cHeight;
            float rx = (float)((Integer)objectInstance.get(VAR_X_POS)).intValue() * width;
            float ry = cHeight - height - (float)((Integer)objectInstance.get(VAR_Y_POS)).intValue() * height;

            int imageWidth = img.getWidth(null);
            int imageHeight = img.getHeight(null);
            float newHeight = 0.9F*height;
            float newWidth = (((float)imageWidth)*newHeight/((float)imageHeight));

            int startx = (int)(rx + width/2 - newWidth/2);
            int starty = (int)(ry + 0.05*newHeight);

            BufferedImage image = img;
            if(StateUtilities.stringOrBoolean(objectInstance.get(VAR_ON_PHONE))) {
                image = phoneImg;
            }

            g2.drawImage(image,startx, starty, (int)newWidth,(int)newHeight,  null);
        }
    }

    public static class DockerPainter implements ObjectPainter {
        protected int dwidth;
        protected int dheight;
        protected int[][] map;
        protected BufferedImage img;
        protected BufferedImage docked;

        public DockerPainter(int[][] map) {
            this.dwidth = map.length;
            this.dheight = map[0].length;
            this.map = map;
            try {
                File f = new File("main/resources/outlet.png");
                this.img = ImageIO.read(f);
            } catch(IOException exception) {
                exception.printStackTrace();
                this.img = null;
            }
        }

        @Override
        public void paintObject(Graphics2D g2, OOState ooState, ObjectInstance objectInstance, float cWidth, float cHeight) {

            float domainXScale = (float)this.dwidth;
            float domainYScale = (float)this.dheight;
            float width = 1.0F / domainXScale * cWidth;
            float height = 1.0F / domainYScale * cHeight;
            float rx = (float)((Integer)objectInstance.get(VAR_X_POS)).intValue() * width;
            float ry = cHeight - height - (float)((Integer)objectInstance.get(VAR_Y_POS)).intValue() * height;

            if(this.img != null) {
                float desiredHeight = 0.25F*height;
                float desiredWidth = this.img.getWidth()*desiredHeight/this.img.getHeight();
                g2.drawImage(this.img,(int)(rx+0.05*width),(int)(ry + 0.95*height - desiredHeight),
                        (int)desiredWidth, (int)desiredHeight,null);
            } else {
                if (StateUtilities.stringOrBoolean(ooState.object(CLASS_ROBOT).get(VAR_DOCKED))) {
                    g2.setColor(Color.green);
                } else {
                    g2.setColor(Color.black);
                }
                Path2D path = new Path2D.Double();
                path.moveTo(rx + 0.05F * width, ry + 0.05 * height);
                path.lineTo(rx + 0.3F * width, ry + 0.05 * height);
                path.lineTo(rx + 0.05 * width, ry + 0.3F * height);
                path.closePath();
                g2.fill(path);
            }
        }
    }

    public static class MessPainter implements ObjectPainter {
        protected int dwidth;
        protected int dheight;
        protected int[][] map;

        protected Map<String, BufferedImage> allMessImages;

        public MessPainter(int[][] map) {
            this.dwidth = map.length;
            this.dheight = map[0].length;
            this.map = map;
            this.allMessImages = new HashMap<>();
        }

        @Override
        public void paintObject(Graphics2D g2, OOState ooState, ObjectInstance objectInstance, float cWidth, float cHeight) {
            GraphicsProperties props = ((VacuumMess)objectInstance).type.graphicsProperties;
            float currDirt = StateUtilities.stringOrNumber(objectInstance.get(VAR_CURR_DIRT)).floatValue();
            float origDirt = (float)((VacuumMess)objectInstance).type.originalDirtiness;

            float domainXScale = (float)this.dwidth;
            float domainYScale = (float)this.dheight;
            float width = 1.0F / domainXScale * cWidth;
            float height = 1.0F / domainYScale * cHeight;
            float rx = (float)((Integer)objectInstance.get(VAR_X_POS)).intValue() * width;
            float ry = cHeight - height - (float)((Integer)objectInstance.get(VAR_Y_POS)).intValue() * height;

            BufferedImage image = null;
            if(props.imagePath != null) {
                if(allMessImages.containsKey(props.imagePath)) {
                    image = allMessImages.get(props.imagePath);
                } else {
                    try {
                        image = ImageIO.read(new File(props.imagePath));
                        allMessImages.put(props.imagePath,image);
                    } catch(IOException e) {
                        allMessImages.put(props.imagePath,null);
                    }
                }
            }

            if(image != null) {
                float origWidth = (float)image.getWidth(null);
                float origHeight = (float)image.getHeight(null);
                float scale = Math.min(0.8F*width/origWidth, 0.2F*height/origHeight);
                float desiredWidth = origWidth*scale;
                float desiredHeight =origHeight*scale;

                Composite comp = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,currDirt/origDirt));
                g2.drawImage(image,(int)(rx+0.1F*width),(int)(ry+height-desiredHeight),(int)desiredWidth, (int)desiredHeight,null);
                g2.setComposite(comp);
            } else {
                Color c = new Color(props.colorR/255F, props.colorG/255F, props.colorB/255F, currDirt/origDirt);
                g2.setColor(c);

                g2.fill(new Float(rx, ry + 0.8F*height, width, 0.2F*height));
            }

        }
    }



    public static class RobotPainter implements ObjectPainter {
        protected int dwidth;
        protected int dheight;
        protected int[][] map;
        protected BufferedImage img;
        protected BufferedImage docked;

        public RobotPainter(int[][] map) {
            this.dwidth = map.length;
            this.dheight = map[0].length;
            this.map = map;

            try {
                File f = new File("main/resources/robot.png");
                this.img = ImageIO.read(f);
            } catch(IOException exception) {
                exception.printStackTrace();
                this.img = null;
            }
            try {
                File f = new File("main/resources/thunderbolt.png");
                this.docked = ImageIO.read(f);
            } catch(IOException exception) {
                exception.printStackTrace();
                this.docked = null;
            }

        }

        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            g2.setColor(Color.gray);
            float domainXScale = (float)this.dwidth;
            float domainYScale = (float)this.dheight;
            float cellWidth = 1.0F / domainXScale * cWidth;
            float cellHeight = 1.0F / domainYScale * cHeight;
            float rx = (float)((Integer)ob.get(VAR_X_POS)).intValue() * cellWidth;
            float ry = cHeight - cellHeight - (float)((Integer)ob.get(VAR_Y_POS)).intValue() * cellHeight;


            if(this.img != null) {
                float desiredWidth = 0.5F*cellWidth;
                float desiredHeight = ((float)this.img.getHeight(null))*desiredWidth/
                        ((float)this.img.getWidth(null));

                g2.drawImage(this.img,(int)(rx+0.25*cellWidth),(int)(ry + cellHeight - desiredHeight),
                        (int)desiredWidth, (int)desiredHeight, null);
            } else {
                g2.fill(new Float(rx + 0.1F * cellWidth, ry + 0.1F * cellHeight, 0.8F * cellWidth, 0.8F * cellHeight));
            }


            float healthProportion = (float)((Integer)ob.get(VAR_HEALTH))/(float)((Integer)ob.get(VAR_MAX_HEALTH));
            g2.setColor(Color.black);
            g2.fill(new Float(rx + 0.2F*cellWidth, ry + 0.2F*cellHeight, 0.1F*cellWidth,0.6F*cellHeight));
            g2.setColor(Color.red);
            g2.fill(new Float(rx + 0.2F*cellWidth, ry + 0.2F*cellHeight + (1.0F-healthProportion)*cellHeight*0.6F, 0.1F*cellWidth,0.6F*cellHeight*healthProportion));

            g2.setColor(Color.black);
            g2.fill(new Float(rx + 0.7F*cellWidth, ry + 0.2F*cellHeight, 0.1F*cellWidth,0.6F*cellHeight));
            g2.setColor(Color.blue);
            float batteryProportion = (float)((Integer)ob.get(VAR_BATTERY))/(float)((Integer)ob.get(VAR_MAX_BATTERY));
            g2.fill(new Float(rx + 0.7F*cellWidth, ry + 0.2F*cellHeight + (1.0F-batteryProportion)*cellHeight*0.6F, 0.1F*cellWidth,0.6F*cellHeight*batteryProportion));


            if (StateUtilities.stringOrBoolean(ob.get(VAR_DOCKED)) && this.docked != null) {
                float thunderboltDesiredWidth = 0.2F*cellWidth;
                float thunderboltDesiredHeight = this.docked.getHeight()*thunderboltDesiredWidth/this.docked.getWidth();
                g2.drawImage(this.docked,(int)(rx+0.4F*cellWidth),(int)(ry+cellHeight/2-thunderboltDesiredHeight/2),
                        (int)thunderboltDesiredWidth,(int)thunderboltDesiredHeight,null);
            }
        }
    }

    public static class CellPainter implements ObjectPainter {
        protected Color col;
        protected int dwidth;
        protected int dheight;
        protected int[][] map;
        protected int shape = 0;

        public CellPainter(Color col, int[][] map) {
            this.col = col;
            this.dwidth = map.length;
            this.dheight = map[0].length;
            this.map = map;
        }

        public CellPainter(int shape, Color col, int[][] map) {
            this.col = col;
            this.dwidth = map.length;
            this.dheight = map[0].length;
            this.map = map;
            this.shape = shape;
        }

        public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {
            g2.setColor(this.col);
            float domainXScale = (float)this.dwidth;
            float domainYScale = (float)this.dheight;
            float width = 1.0F / domainXScale * cWidth;
            float height = 1.0F / domainYScale * cHeight;
            float rx = (float)((Integer)ob.get(VAR_X_POS)).intValue() * width;
            float ry = cHeight - height - (float)((Integer)ob.get(VAR_Y_POS)).intValue() * height;
            if(this.shape == 0) {
                g2.fill(new Float(rx, ry, width, height));
            } else {
                g2.fill(new java.awt.geom.Ellipse2D.Float(rx, ry, width, height));
            }

        }
    }

    public static class MapPainter implements StatePainter {
        protected int dwidth;
        protected int dheight;
        protected int[][] map;

        public MapPainter(int[][] map) {
            this.dwidth = map.length;
            this.dheight = map[0].length;
            this.map = map;
        }

        public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(4.0F));
            float domainXScale = (float)this.dwidth;
            float domainYScale = (float)this.dheight;
            float width = 1.0F / domainXScale * cWidth;
            float height = 1.0F / domainYScale * cHeight;

            for(int i = 0; i < this.dwidth; ++i) {
                for(int j = 0; j < this.dheight; ++j) {
                    boolean drawNorthWall = false;
                    boolean drawEastWall = false;
                    if(this.map[i][j] == 1) {
                        float rx = (float)i * width;
                        float ry = cHeight - height - (float)j * height;
                        g2.fill(new Float(rx, ry, width, height));
                    } else if(this.map[i][j] == 2) {
                        drawNorthWall = true;
                    } else if(this.map[i][j] == 3) {
                        drawEastWall = true;
                    } else if(this.map[i][j] == 4) {
                        drawNorthWall = true;
                        drawEastWall = true;
                    }

                    int left = (int)((float)i * width);
                    int top = (int)(cHeight - height - (float)j * height);
                    if(drawNorthWall) {
                        g2.drawLine(left, top, (int)((float)left + width), top);
                    }

                    if(drawEastWall) {
                        g2.drawLine((int)((float)left + width), top, (int)((float)left + width), (int)((float)top + height));
                    }
                }
            }

        }
    }
}

