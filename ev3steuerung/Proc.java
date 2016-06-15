package ev3steuerung;

/**
 * Beschreiben Sie hier die Klasse Proc.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Proc implements State
{
    Object[] recipe;
    
    public Proc(Object[] rec) {
        this.recipe = rec;
    };

    public int getColor() {
        return 1; // Green flashing
    }
    
    public String getName() {
        return "Proc";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        
        // MQTT STATE INDICATION
        
        
        ev3.led.setPattern(1);
        System.out.println("State: "+getName());
        System.out.println("Working: "+recipe[0]);
        ev3.wait(4000);
        //ev3.audio.systemSound(0);
        System.out.println("I leave - failed");
        ev3.setState(new Maint());
    }
}
