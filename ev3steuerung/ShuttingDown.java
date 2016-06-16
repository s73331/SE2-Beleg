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
        return "Shutting Down";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        
        ev3.led.setPattern(9);
        System.out.println("State: "+getName());
        System.out.println("I shut down");
        ev3.wait(2000);
        
        // STOP MQTT
        ev3.stopMqtt();
        
        
        //EV3_Brick.getInstance().audio.systemSound(2);
        System.out.println("I end this now");
        ev3.wait(1000);
    }
}