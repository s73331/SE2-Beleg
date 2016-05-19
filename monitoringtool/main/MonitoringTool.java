package monitoringtool.main;
import monitoringtool.base.Model;
import monitoringtool.controllers.DebugFrameController;
import monitoringtool.controllers.DebugToggleController;
import monitoringtool.controllers.MonitoringDebugController;
import monitoringtool.controllers.MonitoringEmergencyController;
import monitoringtool.controllers.MonitoringFixController;
import monitoringtool.controllers.MonitoringQueryController;
import monitoringtool.controllers.QueryFrameController;
import monitoringtool.controllers.QueryUpdateController;
import monitoringtool.views.MonitoringView;



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
