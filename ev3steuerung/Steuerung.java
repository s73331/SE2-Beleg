package ev3steuerung;

public class Steuerung {
    
    protected static boolean running;
    
    private Steuerung() {
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
    
    protected static void changeRunning(String reason) {
        if (running)
            running = false;
        else
            running = true;
        EV3_Brick.getInstance().mqttHelper.debug("Running has been changed to "+running);
    } 
    
    public static void main(String[] args) {
        Steuerung m = new Steuerung();
        m.start();
        System.exit(0);
    }
}
