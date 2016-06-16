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
        return "PROC";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        
        
        //ev3.led.setPattern(1);
        //System.out.println("State: "+getName());
        
        try {
            
            // Working and Shit
            boolean workOK = false;
            System.out.println("Working: "+recipe[0]);
            
            if (workOK) {
                ev3.setState(new Idle());
                ev3.mqttHelper.indicateTask(recipe[0].toString(), "done");
            } else {
                ev3.setState(new Maint());
                ev3.mqttHelper.indicateTask(recipe[0].toString(), "abort");
            }
        } catch (Exception e) {
            ev3.setState(new Idle());
            ev3.mqttHelper.indicateTask(recipe[0].toString(), "terminated");
        }
        
        //ev3.wait(4000);
        ////ev3.audio.systemSound(0);
    }
}
