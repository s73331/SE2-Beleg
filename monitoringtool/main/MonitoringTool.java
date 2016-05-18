package main;
import base.Model;
import views.MonitoringView;

import controllers.DebugFrameController;
import controllers.DebugToggleController;
import controllers.MonitoringDebugController;
import controllers.MonitoringEmergencyController;
import controllers.MonitoringFixController;
import controllers.MonitoringQueryController;
import controllers.QueryFrameController;
import controllers.QueryUpdateController;


/**
 * Monitoring Tool
 * 
 * @author martin
 *
 */
public class MonitoringTool {
    public MonitoringTool() {
        Model model=Model.getModel();
        MonitoringView view=new MonitoringView(model);
        view.addDebugController(new MonitoringDebugController(model,view.getDebugView()));
        view.addQueryController(new MonitoringQueryController(model,view.getQueryView()));
        view.addFixController(new MonitoringFixController(model,view));
        view.addEmergencyController(new MonitoringEmergencyController(model, view));
        view.addQueryUpdateController(new QueryUpdateController(model, view.getQueryView()));
        view.addDebugToggleController(new DebugToggleController(model, view.getDebugView()));
        view.getQueryView().addFrameListener(new QueryFrameController(model));
        view.getDebugView().addFrameListener(new DebugFrameController(model));
    }
    
    /**
     * Hauptprogramm.
     * @param args keine Kommandozeilenparameter
     */
    public static void main(String[] args) {
        new MonitoringTool();
    }
}
