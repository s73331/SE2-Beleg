package controllers;
import java.awt.event.ActionEvent;

import base.Controller;
import base.Model;
import base.View;

/**
 * Controller, which makes the EV3 transition from state maintenance to idle
 * 
 * @author martin
 *
 */
public class MonitoringFixController extends Controller {
    public MonitoringFixController(Model model, View monitoringView) {
        super(model, monitoringView);
    }

    public void actionPerformed(ActionEvent ae) {
        // TODO invoke state transition from maint to idle
    }

}
