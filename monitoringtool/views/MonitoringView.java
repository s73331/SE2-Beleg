package monitoringtool.views;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;

import javax.swing.*;

import monitoringtool.base.Controller;
import monitoringtool.base.Model;
import monitoringtool.base.View;
import monitoringtool.controllers.MonitoringToolCloser;



/**
 * 
 * @author martin
 *
 */
public class MonitoringView extends JPanel implements View {
    private static final long serialVersionUID = -8754066383077022418L;
    private JButton debugButton, fixButton, queryButton, emergencyButton;
    private QueryView queryView;
    private DebugView debugView;
    private MonitoringInformationView monitoringInformationView;
    private JFrame frame;
    public MonitoringView(Model model) {
        queryView=new QueryView(model);
        debugView=new DebugView(model);
        
        //set layout to fit information at top and buttons at bottom
        setLayout(new GridLayout(2,1));
        
        //add information to the top
        monitoringInformationView=new MonitoringInformationView(model);
        add(monitoringInformationView);
        
        //prepare buttonPanel and add it to the bottom
        JPanel buttonPanel=new JPanel();
        buttonPanel.setLayout(new GridLayout(2,2));
        debugButton=new JButton("Debug");
        fixButton=new JButton("Entstören");
        queryButton=new JButton("SQL-Abfragen");
        emergencyButton=new JButton("Not-Aus");
        buttonPanel.add(debugButton);
        buttonPanel.add(fixButton);
        buttonPanel.add(queryButton);
        buttonPanel.add(emergencyButton);
        add(buttonPanel);
        
        //make frame
        setPreferredSize(new Dimension(600,400));
        frame=new JFrame("Monitoring Tool - Gerät "+model.getDeviceID());
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new MonitoringToolCloser());
    }
    public QueryView getQueryView() {
        return queryView;
    }
    public DebugView getDebugView() {
        return debugView;
    }
    public MonitoringInformationView getMonitoringInformationView() {
        return monitoringInformationView;
    }
    public void addDebugController(Controller controller) {
        debugButton.addActionListener(controller);
    }
    public void addQueryController(Controller controller) {
        queryButton.addActionListener(controller);
    }
    public void addFixController(Controller controller) {
        fixButton.addActionListener(controller);
    }
    public void addEmergencyController(Controller controller) {
        emergencyButton.addActionListener(controller);
    }
    public void addQueryUpdateController(Controller controller) {
        queryView.addQueryUpdateListener(controller);
    }
    public void addDebugToggleController(Controller controller) {
        debugView.addDebugToggleListener(controller);
    }
    public void addMonitoringToolCloser(WindowAdapter w) {
        frame.addWindowListener(w);
    }
    public void update() {
        queryView.update();
        debugView.update();
        monitoringInformationView.update();
    }
}
