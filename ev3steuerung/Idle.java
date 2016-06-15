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
        return "Idle";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        
        // MQTT STATE INDICATION
        
        ev3.led.setPattern(3);
        System.out.println("State: "+getName());
        // ev3.audio.systemSound(0);
        ev3.wait(4000);
        System.out.println("I leave");
        
        // TODO: RECIPE-STRUCTURE TO BE NOTED HERE
        ev3.setState(new Proc(new Object[]{"Recipe-1"}));
    }
}
