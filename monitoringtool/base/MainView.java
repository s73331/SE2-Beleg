package monitoringtool.base;

import java.awt.event.WindowAdapter;


public interface MainView extends View {
    public void addDebugController(Controller controller);
    public void addQueryController(Controller controller);
    public void addFixController(Controller controller);
    public void addEmergencyController(Controller controller);
    public void addQueryUpdateController(Controller controller);
    public void addDebugToggleController(Controller controller);
    public void addMonitoringToolCloser(WindowAdapter w);
}
