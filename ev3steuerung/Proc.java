package ev3steuerung;

import ev3steuerung.rezeptabarbeitung.Recipe;

/**
 * State Pattern Entity in MES-StateDiagram
 * 
 * @author Christoph Schmidt
 * @version 0.9
 * @see Idle
 * @see Maint
 * @see Recipe
 */
public class Proc implements State {
    public Proc() { }

    public int getColor() {
        return 1; // Green flashing
    }
    
    public String getName() {
        return "PROC";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.led.setPattern(1);
        ev3.getMqttHelper().debug("Start of Proc");
        
        // MQTT STATE INDICATION
        ev3.getMqttHelper().indicateState(this.getName());
        ev3.getMqttHelper().publishState();
        System.out.println("-> "+getName());
        
        // Recipe checking
        ev3.getMqttHelper().debug("Getting the next Recipe");
        Recipe recipe;
        recipe = ev3.getNextRecipe();
        if (recipe == null) {
            ev3.setState(new Maint(),false);
            ev3.getMqttHelper().debug("Recipe failed to load");
            return;
        }
        ev3.getMqttHelper().debug("Recipe loaded");
        System.out.println("-> "+getName()+": produce "+recipe);
        try {
            // Actually Working
            ev3.getMqttHelper().debug("Register Recipe");
            recipe.register();
            ev3.getMqttHelper().debug("Working Recipe");
            boolean workOK = recipe.work();
            ev3.getMqttHelper().debug("Work ended: "+workOK);
            ev3.getMqttHelper().debug("Close Recipe");
            
            if (workOK) {
                ev3.setState(new Idle(),false);
                ev3.getMqttHelper().debug("Work has been done");
                ev3.getMqttHelper().indicateTask(recipe.toString(), "done");
            } else {
                ev3.setState(new Idle(),false);
                ev3.getMqttHelper().debug("Work has been terminated");
                ev3.getMqttHelper().indicateTask(recipe.toString(), "terminated");
            }
        } catch (InterruptedException e) {
            ev3.setState(new Maint(),false);
            ev3.getMqttHelper().debug("Work has been aborted");
            ev3.getMqttHelper().indicateTask(recipe.toString(), "abort");
        } catch (lejos.hardware.DeviceException de) {
            de.printStackTrace();
            ev3.setState(new Maint(),false);
            ev3.getMqttHelper().debug("Problem Registering Ports on EV3");
            ev3.getMqttHelper().indicateTask(recipe.toString(), "abort");
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            ev3.setState(new Maint(),false);
            ev3.getMqttHelper().debug("Problem Registering a specific Device on EV3");
            ev3.getMqttHelper().indicateTask(recipe.toString(), "abort");
        } finally {
            ev3.waiting = true;
            ev3.getMqttHelper().debug("Closing Recipe");
            recipe.close();
        }
        
        boolean mqttConfirm = false;
        // WAIT FOR NON-CONFIRM
        ev3.getMqttHelper().debug("Waiting for confirm");
        for (int i = 0; i < ev3.TASKCONF_TIMEOUT && !mqttConfirm; i++) {
            if (ev3.isConfirmed()) {
                mqttConfirm = true;
                ev3.getMqttHelper().debug("Confirm acknowleged");
            }
            else {
                try {
                    ev3.getMqttHelper().debug("Waiting longer for confirm");
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ev3.getMqttHelper().debug("Confirm Wait InterruptedException recieved");
                    ie.printStackTrace();
                }
            }
        }
        ev3.waiting = false;
        
        if (!mqttConfirm) {
            ev3.setState(new ShuttingDown(),false);
            ev3.getMqttHelper().debug("Maximum Confirm waiting time passed");
        }
        
        ev3.getMqttHelper().debug("End of Proc");
    }
}
