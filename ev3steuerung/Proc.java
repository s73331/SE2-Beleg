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
        ev3.led.setPattern(1);
        ev3.mqttHelper.debug("Start of Proc");
        
        // MQTT STATE INDICATION
        ev3.mqttHelper.indicateState(this.getName());
        ev3.mqttHelper.publishState();
        System.out.println("-> "+getName());
        
        // Rezept überprüfen
        ev3.mqttHelper.debug("Getting the next Recipe");
        Recipe recipe;
        if (ev3.recName.isEmpty()) {
            ev3.setState(new Maint(),false);
            ev3.mqttHelper.debug("Recipe name is not set");
            return;
        } else{
            recipe = ev3.loadRecipe(ev3.recName);
            if (recipe == null) {
                ev3.setState(new Maint(),false);
                ev3.mqttHelper.debug("Recipe failed to load");
                return;
            }
            ev3.mqttHelper.debug("Recipe loaded");
        }
        System.out.println("-> "+getName()+": produce "+ev3.recName);
        try {
            // Working and Shit
            ev3.mqttHelper.debug("Register Recipe");
            recipe.register();
            ev3.mqttHelper.debug("Working Recipe");
            boolean workOK = recipe.work();
            ev3.mqttHelper.debug("Work ended: "+workOK);
            ev3.mqttHelper.debug("Close Recipe");
            recipe.close();
            
            if (workOK) {
                ev3.setState(new Idle(),false);
                ev3.mqttHelper.debug("Work has been done");
                ev3.mqttHelper.indicateTask(recipe.toString(), "done");
            } else {
                ev3.setState(new Maint(),false);
                ev3.mqttHelper.debug("Work has been aborted");
                ev3.mqttHelper.indicateTask(recipe.toString(), "abort");
            }
        } catch (InterruptedException e) {
            ev3.setState(new Idle(),false);
            ev3.mqttHelper.debug("Work has been terminated");
            ev3.mqttHelper.indicateTask(recipe.toString(), "terminated");
        } catch (lejos.hardware.DeviceException de) {
            ev3.setState(new Maint(),false);
            ev3.mqttHelper.debug("Problem Registering Ports on EV3");
            ev3.mqttHelper.indicateTask(recipe.toString(), "abort");
        }
        
        boolean mqttConfirm = false;
        // WAIT FOR NON-CONFIRM
        ev3.mqttHelper.debug("Waiting for confirm");
        ev3.waiting = true;
        for (int i = 0; i < ev3.TASKCONF_TIMEOUT && !mqttConfirm; i++) {
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
        ev3.waiting = false;
        
        if (!mqttConfirm) {
            ev3.setState(new ShuttingDown(),false);
            ev3.mqttHelper.debug("Maximum Confirm waiting time passed");
        }
        
        ev3.mqttHelper.debug("End of Proc");
    }
}
