package ev3steuerung;

/**
 * Control is the Main-Class of the EV3-Steuerung.
 * Here it begins and it ends.
 * 
 * @author Christoph Schmidt
 * @version 0.8
 * @since 01.04.2016 */
 
public class Control {
    private boolean running;
    
    private Control () {
       running = true;
    }
    
    private void start() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.mqttHelper.debug("STRG: Start of the Machine");
        while (running) {
            if (ev3.getState() instanceof ShuttingDown)
                running = false;
            ev3.getState().doAction();
        }
        ev3.mqttHelper.debug("STRG: End of the Machine");
        ev3.stopMqtt();
    }
    
    /**
     * Starts the EV3-Steuerung without need of Arguments
     * 
     * @see EV3_Brick
     * @param args - Arguments of the MAIN (not needed) */
    public static void main(String[] args) {
        Control m = new Control();
        m.start();
        System.exit(1);
    }
}