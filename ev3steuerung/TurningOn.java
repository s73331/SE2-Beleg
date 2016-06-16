package ev3steuerung;

/**
 * Beschreiben Sie hier die Klasse TurnOn.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class TurningOn implements State
{
    public TurningOn() {   };

    public int getColor() {
        return 4; // Green flashing
    }
    
    public String getName() {
        return "TURNING_ON";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        
        // Set Brick-Colors
        //ev3.led.setPattern(4);
        ////ev3.audio.systemSound(3);
        
        // Tell where you at
        System.out.println("State: "+getName());    //MQTT Message DEBUG
        System.out.println("I work");
        
        // FUN STUFF
        System.out.println("Press 2 Continue");
        //ev3.waitForButtonPress("any");
        
        // Get the Boolean Value if the Recipes are loaded or not
        boolean recLoaded = ev3.loadRecipes();
        
        // If the Recipes are loaded and MQTT Connection is established, we continue as usual
        if (!recLoaded)
            ev3.setState(new ShuttingDown());
        else {
            ev3.setState(new Idle());
            
            try {
                ev3.startMqtt();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            ev3.mqttHelper.register();
            
            boolean mqttConfirm = false;
            // WAIT FOR NON-CONFIRM
            for (int i = 0; i < 3 && !mqttConfirm; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                if (ev3.isConfirmed())
                    mqttConfirm = true;
            }
            
            if (!mqttConfirm) {
                ev3.setState(new ShuttingDown());
            }
        }
    }
}
