package controllers;
import java.awt.event.ActionEvent;

import base.Controller;
import base.Model;
import base.View;

/**
 * Controller, which makes a button show and hide the debug frame.
 * 
 * @author martin
 *
 */
public class MonitoringDebugController extends Controller {
    public MonitoringDebugController(Model model, View debugView) {
        super(model, debugView);
    }
    public void actionPerformed(ActionEvent ae) {
        model.toggleDebugOpened();
        view.update();
    }
}
