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
        ev3.mqttHelper.debug("Start of Maint");
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        
        //ev3.led.setPattern(5);
        ev3.mqttHelper.debug("State: "+getName());
        //ev3.wait(4000);
        ////ev3.audio.systemSound(1);
        
        System.out.println("Wait 2 Entstör");
        //ev3.waitForButtonPress("any");
        
        boolean errorMode = true;
        ev3.mqttHelper.debug("Waiting for manual fix");
        for (int i = 0; i < 10 && errorMode; i++) {
            if (ev3.isFixed()) {
                ev3.mqttHelper.debug("Manual Fix acknowledged");
                errorMode = false;
            } else {
                try {
                    ev3.mqttHelper.debug("Waiting longer for manual fix");
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ev3.mqttHelper.debug("Interrupted Exception recieved");
                    ie.printStackTrace();
                }
            }
        }
        
        if (!errorMode) {
            ev3.mqttHelper.debug("Manual Fix applied");
            ev3.setState(new Idle());
        } else {
            ev3.mqttHelper.debug("Maximum fix wait time has passed");
            ev3.setState(new ShuttingDown());
        }
        
        ev3.mqttHelper.debug("End of Maint");
    }
}
