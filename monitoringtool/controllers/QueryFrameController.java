package monitoringtool.controllers;

import java.awt.event.WindowEvent;

import monitoringtool.base.FrameController;
import monitoringtool.base.Model;


/**
 * Controller, which synchronizes the query frame state to the model when listening to it.
 * 
 * @author martin
 *
 */
public class QueryFrameController extends FrameController {
    public QueryFrameController(Model model) {
        super(model);
    }
    public void windowClosing(WindowEvent we) {
        model.toggleQueriesOpened();
    }
}
