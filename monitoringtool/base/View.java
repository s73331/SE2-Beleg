package monitoringtool.base;

import javax.swing.JPanel;

/**
 * Abstract base class for a view in the MVC pattern.
 * 
 * @author martin
 *
 */
public abstract class View extends JPanel {
    private static final long serialVersionUID = 311437091987194539L;
    protected Model model;
    /**
     * Only constructor.
     * 
     * @param model Model
     */
    public View(Model model) {
        if(model==null) throw new IllegalArgumentException("model must not be null");
        this.model=model;
    }
    /**
     * Method used to update this view and all subviews.
     */
    public abstract void update();
}
