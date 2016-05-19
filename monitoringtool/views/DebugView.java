package monitoringtool.views;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import monitoringtool.base.Controller;
import monitoringtool.base.FrameController;
import monitoringtool.base.Model;
import monitoringtool.base.View;


/**
 * 
 * @author martin
 *
 */
public class DebugView extends JPanel implements View {
    private static final long serialVersionUID = 7326178942021896621L;
    private JFrame frame;
    private JButton toggleButton;
    private JLabel state;
    private JTextArea debugLog;
    private Model model;
    public DebugView(Model model) {
        this.model=model;
        setLayout(new BorderLayout());
        JPanel centerPanel=new JPanel();
        add(centerPanel, BorderLayout.NORTH);
        centerPanel.add(new JLabel("Debug Mode: "));
        state=new JLabel();
        centerPanel.add(state);
        toggleButton=new JButton("Toggle");
        centerPanel.add(toggleButton, BorderLayout.CENTER);
        debugLog=new JTextArea();
        add(debugLog, BorderLayout.CENTER);
        setPreferredSize(new Dimension(500,500));
        frame=new JFrame("Debug - Gerät "+model.getDeviceID());
        frame.add(this);
        frame.pack();
        update();
    }
    public void update() {
        if(model.isDebugOpened()) {
            frame.setVisible(true);
        } else {
            frame.setVisible(false);
        }        
        if(model.isDebugging()) state.setText("on");
        else state.setText("off");
        debugLog.setText(model.getDebugLog());
    }
    public void addFrameListener(FrameController controller) {
        frame.addWindowListener(controller);
    }
    public void addDebugToggleListener(Controller controller) {
        toggleButton.addActionListener(controller);
    }
}
