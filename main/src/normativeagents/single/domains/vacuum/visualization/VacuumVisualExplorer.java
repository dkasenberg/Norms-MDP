package normativeagents.single.domains.vacuum.visualization;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.shell.EnvironmentShell;
import burlap.shell.visual.TextAreaStreams;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;
import normativeagents.single.domains.vacuum.state.VacuumState;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by dkasenberg on 9/7/17.
 */
public class VacuumVisualExplorer extends VisualExplorer {

    public VacuumVisualExplorer(SADomain domain, Visualizer painter, State baseState) {
        super(domain, painter, baseState);
    }

    protected void executeAction(Action ga) {
        if(domain.getAction(ga.actionName()).allApplicableActions(env.currentObservation()).isEmpty()) {
            System.out.println("Action not allowed in this state: " + ga.toString());
            return;
        }
        EnvironmentOutcome eo = this.env.executeAction(ga);
        try {
            new Yaml().dump(eo.op, new FileWriter("main/resources/currentState.yaml"));
        } catch(IOException e) {
            System.out.println(new Yaml().dump(eo.op));
        }
        this.updateState(eo.op);
    }

    @Override
    protected void updatePropTextArea(State s) {
        if(s instanceof VacuumState) {
            this.propViewer.setText(s.toString());
        }
    }

    public void initGUI() {
        this.painter.setPreferredSize(new Dimension(this.cWidth, this.cHeight));
        this.propViewer.setPreferredSize(new Dimension(this.cWidth, 500));
        Container bottomContainer = new Container();
        bottomContainer.setLayout(new BorderLayout());
        bottomContainer.add(this.propViewer, "North");
        this.getContentPane().add(bottomContainer, "South");
        this.getContentPane().add(this.painter, "Center");
        this.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
                VacuumVisualExplorer.this.handleKeyPressed(e);
            }
        });
        this.painter.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
                VacuumVisualExplorer.this.handleKeyPressed(e);
            }
        });
        this.propViewer.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
                VacuumVisualExplorer.this.handleKeyPressed(e);
            }
        });
        this.actionField = new TextField(20);
        bottomContainer.add(this.actionField, "Center");
        this.actionButton = new JButton("Execute");
        this.actionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                VacuumVisualExplorer.this.handleExecute();
            }
        });
        bottomContainer.add(this.actionButton, "East");
        this.painter.updateState(this.env.currentObservation());
        this.updatePropTextArea(this.env.currentObservation());
        JButton showConsoleButton = new JButton("Show Shell");
        showConsoleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                VacuumVisualExplorer.this.consoleFrame.setVisible(true);
            }
        });
        bottomContainer.add(showConsoleButton, "South");
        this.consoleFrame = new JFrame();
        this.consoleFrame.setPreferredSize(new Dimension(600, 500));
        this.stateConsole = new JTextArea(40, 40);
        this.stateConsole.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret)this.stateConsole.getCaret();
        caret.setUpdatePolicy(2);
        this.stateConsole.setEditable(false);
        this.stateConsole.setMargin(new Insets(10, 5, 10, 5));
        JScrollPane shellScroll = new JScrollPane(this.stateConsole, 22, 31);
        this.consoleFrame.getContentPane().add(shellScroll, "Center");
        this.tstreams = new TextAreaStreams(this.stateConsole);
        this.shell = new EnvironmentShell(this.domain, this.env, this.tstreams.getTin(), new PrintStream(this.tstreams.getTout()));
        this.shell.addObservers(this);
        this.shell.setVisualizer(this.painter);
        this.shell.addCommand(new VisualExplorer.LivePollCommand());
        this.shell.start();
        final JTextField consoleCommand = new JTextField(40);
        consoleCommand.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String command = ((JTextField)e.getSource()).getText();
                consoleCommand.setText("");
                VacuumVisualExplorer.this.tstreams.receiveInput(command + "\n");
            }
        });
        this.consoleFrame.getContentPane().add(consoleCommand, "South");
        this.pack();
        this.setVisible(true);
        this.consoleFrame.pack();
        this.consoleFrame.setVisible(false);
    }
}
