package controllers;
import java.awt.event.ActionEvent;

import base.Controller;
import base.Model;
import base.View;

/**
 * Controller, which makes a button initiate a emergency shutdown of the connected EV3.
 * 
 * @author martin
 *
 */
public class MonitoringEmergencyController extends Controller {
    public MonitoringEmergencyController(Model model, View monitoringView) {
        super(model, monitoringView);
    }

    public void actionPerformed(ActionEvent arg0) {
        // TODO emergency shutdown        
    }

}
