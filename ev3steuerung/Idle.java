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
        
        System.out.println("State: "+getName());
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        
        //ev3.led.setPattern(3);
        // //ev3.audio.systemSound(0);
        //ev3.wait(4000);
        
        System.out.println("I need work");
        
        // MQTT Request Task
        boolean produce = false;
        // WAIT FOR NON-CONFIRM
        
        for (int i = 0; i < 5 && !produce; i++) {
            ev3.mqttHelper.requestTask();
            if (ev3.isSleep())
                i = 0;
            if (ev3.isProduce()) {
                produce = true;
                ev3.setState(new Proc(new Object[]{ev3.nextRecipe()})); //TODO: RECIPE STRUCTURES
            }
            else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }  
        }
        
        // If There was neither a Sleep, nor a produce or the TIme as run out
        if (!produce)
            ev3.setState(new ShuttingDown());
            
        System.out.println("I leave");
    }
}
