package ev3steuerung;

public class Steuerung {
    
    protected static boolean running;
    
    private Steuerung() {
       running = true;
    }
    
    private void start() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        while (running) {
            if (ev3.getState() instanceof ShuttingDown)
                running = false;
            ev3.getState().doAction();
        }
    }
    
    protected static void changeRunning() {
        if (running)
            running = false;
        else
            running = true;
        System.out.println("Running has been changed to "+running); //MQTT Message
    } 
    
    public static void main(String[] args) {
        System.out.println("The Brick has started to work");  //MQTT MESSAGE
        
        Steuerung m = new Steuerung();
        m.start();
        
        System.out.println("The Brick has shut down");  //MQTT MESSAGE
        System.exit(0);
    }
}
