package ev3steuerung;

/**
 * Beschreiben Sie hier die Klasse TurnOff.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class ShuttingDown implements State
{
    public ShuttingDown() {   };

    public int getColor() {
        return 6; // Green flashing
    }
    
    public String getName() {
        return "DOWN";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.mqttHelper.debug("Start of ShuttingDown");
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        
        //ev3.led.setPattern(9);
        ev3.mqttHelper.debug("State: "+getName());
        
        //EV3_Brick.getInstance().audio.systemSound(2);
        ev3.mqttHelper.debug("End of ShuttingDown");
        //ev3.wait(1000);
    }
}
