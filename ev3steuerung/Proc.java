package ev3steuerung;

import ev3steuerung.rezeptabarbeitung.Recipe;
/**
 * Beschreiben Sie hier die Klasse Proc.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class Proc implements State
{
    public Proc() {
        
    };

    public int getColor() {
        return 1; // Green flashing
    }
    
    public String getName() {
        return "PROC";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.mqttHelper.debug("Start of Proc");
        
        // Rezept übeprüfen
        ev3.mqttHelper.debug("Getting the next Recipe");
        Recipe recipe = ev3.getNextRecipe();
        if (recipe == null) {
            ev3.setState(new Maint());
            ev3.mqttHelper.debug("Recipe failed to load");
            return;
        }
        ev3.mqttHelper.debug("Recipe loaded");
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        
        //ev3.led.setPattern(1);
        ev3.mqttHelper.debug("State: "+getName());
        
        try {
            // Working and Shit
            boolean workOK = false;
            ev3.mqttHelper.debug("Working on: "+recipe);
            
            if (workOK) {
                ev3.setState(new Idle());
                ev3.mqttHelper.debug("Work has been done");
                ev3.mqttHelper.indicateTask(recipe.toString(), "done");
            } else {
                ev3.setState(new Maint());
                ev3.mqttHelper.debug("Work has been aborted");
                ev3.mqttHelper.indicateTask(recipe.toString(), "abort");
            }
        } catch (Exception e) {
            ev3.setState(new Idle());
            ev3.mqttHelper.debug("Work has been terminated");
            ev3.mqttHelper.indicateTask(recipe.toString(), "terminated");
        }
        
        boolean mqttConfirm = false;
        // WAIT FOR NON-CONFIRM
        ev3.mqttHelper.debug("Waiting for confirm");
        for (int i = 0; i < 3 && !mqttConfirm; i++) {
            if (ev3.isConfirmed()) {
                mqttConfirm = true;
                ev3.mqttHelper.debug("Confirm acknowleged");
            }
            else {
                try {
                    ev3.mqttHelper.debug("Waiting longer for confirm");
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ev3.mqttHelper.debug("Confirm Wait InterruptedException recieved");
                    ie.printStackTrace();
                }
            }
        }
        
        if (!mqttConfirm) {
            ev3.setState(new ShuttingDown());
            ev3.mqttHelper.debug("Maximum Confirm waiting time passed");
        }
        
        ev3.mqttHelper.debug("End of Proc");
    }
}
