package ev3steuerung;

/**
 * Beschreiben Sie hier die Klasse Idle.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Idle implements State
{
   
    public Idle() {   };

    public int getColor() {
        return 3; // Green flashing
    }
    
    public String getName() {
        return "IDLE";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.mqttHelper.debug("Start of Idle");
        ev3.mqttHelper.publishState();
        
        ev3.mqttHelper.debug("State: "+getName());
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        
        //ev3.led.setPattern(3);
        // //ev3.audio.systemSound(0);
        //ev3.wait(4000);
        
        boolean produce = false;
        
        // WAIT FOR NON-CONFIRM
        ev3.mqttHelper.debug("Waiting for produce-task or sleep");
        ev3.waiting = true;
        for (int i = 0; i < ev3.TASKREQ_TIMEOUT && !produce; i++) {
            if (ev3.isSleep()) {
                i = 0;
                ev3.mqttHelper.debug("Sleep acknowlegded, Resetting Wait time");
            }
            if (ev3.isProduce()) {
                produce = true;
                ev3.setState(new Proc(),false); //TODO: RECIPE STRUCTURES
                ev3.mqttHelper.debug("Produce acknowlegded");
            }
            else {
                ev3.mqttHelper.requestTask();
                try {
                    ev3.mqttHelper.debug("Waiting longer for produce or sleep");
                    Thread.sleep(1000*ev3.SLEEP_TIME);
                } catch (InterruptedException ie) {
                    ev3.mqttHelper.debug("sleep/produce Wait InterruptedException recieved");
                    ie.printStackTrace();
                }
            }
        }
        ev3.waiting = false;
        
        // If There was neither a Sleep, nor a produce or the TIme as run out
        if (!produce) {
            ev3.setState(new ShuttingDown(),false);
            ev3.mqttHelper.debug("Maximum produce or sleep wait time has passed");
        }
        
        ev3.mqttHelper.debug("End of Idle");
    }
}
