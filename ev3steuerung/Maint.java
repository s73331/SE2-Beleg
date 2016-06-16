package ev3steuerung;

/**
 * Beschreiben Sie hier die Klasse Maint.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Maint implements State
{
    public Maint() {   };

    public int getColor() {
        return 5; // Green flashing
    }
    
    public String getName() {
        return "MAINT";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        
        //ev3.led.setPattern(5);
        System.out.println("State: "+getName());
        System.out.println("I work");
        //ev3.wait(4000);
        ////ev3.audio.systemSound(1);
        
        System.out.println("Wait 2 Entstör");
        //ev3.waitForButtonPress("any");
        
        boolean errorMode = true;
        
        for (int i = 0; i < 10 && errorMode; i++) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            if (ev3.isFixed()) {
                errorMode = false;
            }
        }
        
        System.out.println("I leave");
        
        if (!errorMode) {
            ev3.setState(new Idle());
        } else {
            ev3.setState(new ShuttingDown());
        }
    }
}
