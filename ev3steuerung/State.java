package ev3steuerung; 

/**
 * Interface State brings all functionality of the State-Pattern
 * 
 * @author Christoph Schmidt
 * @version 0.1 */

public interface State {
    
    /**
     * Returns the State Name
     * @return The String representation of the State */
    String getName();
    
    /**
     * Starts the Work in the State */
    void doAction();
    
    /**
     * Returns the int-code of the color-value this state is supposed to show
     * @see EV3 LED-Color Scheme
     * @return The Integer representative of the Color */
    int getColor();
}
