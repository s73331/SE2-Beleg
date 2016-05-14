package controllers;
import java.awt.event.ActionEvent;

import base.Controller;
import base.Model;
import base.View;

/**
 * Controller, which makes a button updates the query.
 * 
 * @author martin
 *
 */
public class QueryUpdateController extends Controller {

    public QueryUpdateController(Model model, View queryView) {
        super(model, queryView);
    }

    public void actionPerformed(ActionEvent ae) {
        model.updateQuery();
        view.update();
    }

}
