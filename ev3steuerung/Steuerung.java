package ev3steuerung;

public class Steuerung {
    
    protected static boolean running;
    
    protected Steuerung() {
       running = true;
       start();
    }
    
    private void start() {
        while (running) {
            if (EV3_Brick.getInstance().getState() instanceof ShuttingDown)
                running = false;
            EV3_Brick.getInstance().getState().doAction();
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
        System.out.println("The Brick has shut down");  //MQTT MESSAGE
        
        Steuerung m = new Steuerung();
        
        System.out.println("The Brick has shut down");  //MQTT MESSAGE
    }
}
