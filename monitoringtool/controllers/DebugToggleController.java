package controllers;
import java.awt.event.ActionEvent;

import base.Controller;
import base.Model;
import base.View;

/**
 * Controller, which makes a button toggle the debug mode when listening to it.
 * 
 * @author martin
 *
 */
public class DebugToggleController extends Controller {
    public DebugToggleController(Model model, View debugView) {
        super(model, debugView);
    }
    public void actionPerformed(ActionEvent ae) {
        model.toggleDebug();
        view.update();
    }
}
