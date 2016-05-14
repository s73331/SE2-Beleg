package base;

import java.awt.event.WindowAdapter;

/**
 * Abstract base class for a FrameController, useful to sync the state transition of a frame to the model when closing the frame with the exit button.
 * Override the method windowClosing(WindowEvent e) with model.toggleXYZopened();
 * 
 * @author martin
 *
 */
public abstract class FrameController extends WindowAdapter {
    protected Model model;
    /**
     * Only constructor for FrameController
     * @param model Model
     */
    public FrameController(Model model) {
        if(model==null) throw new IllegalArgumentException("model must not be null");
        this.model=model;
    }
}
