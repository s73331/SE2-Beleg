package monitoringtool.base;

/**
 * Abstract base class for a view in the MVC pattern.
 * 
 * @author martin
 *
 */
public interface View {
    /**
     * Method used to update this view and all subviews.
     */
    public abstract void update();
}
