package monitoringtool.controllers;
import java.awt.event.ActionEvent;

import monitoringtool.base.Controller;
import monitoringtool.base.Model;
import monitoringtool.base.View;


/**
 * Controller, which makes the button show and hide the query frame.
 * 
 * @author martin
 *
 */
public class MonitoringQueryController extends Controller {
    public MonitoringQueryController(Model model, View queryView) {
        super(model, queryView);
    }
    public void actionPerformed(ActionEvent ae) {
        model.toggleQueriesOpened();
        view.update();
    }
}
