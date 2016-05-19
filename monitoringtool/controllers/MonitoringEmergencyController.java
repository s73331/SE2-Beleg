package monitoringtool.controllers;
import java.awt.event.ActionEvent;

import monitoringtool.base.Controller;
import monitoringtool.base.Model;
import monitoringtool.base.View;


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
