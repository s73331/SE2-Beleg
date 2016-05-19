package monitoringtool.controllers;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Controller, which makes all frames close when the frame is closed.
 * Should obviously be listening to the main frame.
 * 
 * @author martin
 *
 */
public class MonitoringToolCloser extends WindowAdapter {
        public void windowClosing(WindowEvent e){
            System.exit(0);
        }
}
