package base;
import java.awt.event.ActionListener;

/**
 * Abstract base class for a controller according to the MVC pattern.
 *  
 * @author martin
 *
 */
public abstract class Controller implements ActionListener {
    protected Model model;
    protected View view;
    /**
     * Only constructor for Controller.
     * @param model Model, as returned my Model.getModel()
     * @param view View
     */
    public Controller(Model model, View view) {
        if(model==null) throw new IllegalArgumentException("model must not be null");
        if(view==null) throw new IllegalArgumentException("view must not be null");
        this.model=model;
        this.view=view;
    }
}
