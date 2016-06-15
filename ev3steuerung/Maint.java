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
        return "Maint";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        
        ev3.led.setPattern(5);
        System.out.println("State: "+getName());
        System.out.println("I work");
        ev3.wait(4000);
        //ev3.audio.systemSound(1);
        
        System.out.println("Press 2 Entstör");
        ev3.waitForButtonPress("any");
        
        System.out.println("I leave");
        ev3.setState(new ShuttingDown());
    }
}
