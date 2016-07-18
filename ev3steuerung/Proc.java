package ev3steuerung;

import java.util.ArrayDeque;

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
public class Proc implements State
{
    public int getColor() {
        return 1; // Green flashing
    }
    
    public String getName() {
        return "PROC";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.getLED().setPattern(1);
        ev3.getMqttHelper().debug("Start of Proc");
        
        // MQTT STATE INDICATION
        ev3.getMqttHelper().indicateState(this.getName());
        ev3.getMqttHelper().publishState();
        System.out.println("-> "+getName());
        
        // Recipe checking
        ev3.getMqttHelper().debug("Load RecipeID");
        String recipeID = ev3.getrecipeID();
        
        //Recipe Loading
        ArrayDeque<Object[]> recipeDeque = ev3.getRecipe(recipeID);
        
        if (recipeDeque == null)
        {
          ev3.setState(new Maint(),false);
          ev3.getMqttHelper().debug("Recipe failed to load");
          return;
        }
        recipeDeque = recipeDeque.clone();
        
        Recipe recipe = new Recipe(recipeDeque,recipeID);
                
        if( !recipe.checkConfiguration()){
            ev3.setState(new Maint(),false);
            ev3.getMqttHelper().debug("Ev3 has not the correct Devices for this recipe");
            ev3.getMqttHelper().indicateTask(recipeID, "abort");
            ev3.setWaiting(true);
            ev3.getMqttHelper().debug("Closing Recipe");
        }else{
        
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
	                ev3.getMqttHelper().indicateTask(recipeID, "done");
	            } else {
	                ev3.setState(new Idle(),false);
	                ev3.getMqttHelper().debug("Work has been terminated");
	                ev3.getMqttHelper().indicateTask(recipeID, "terminated");
	            }
	        } catch (InterruptedException e) {
	            ev3.setState(new Maint(),false);
	            ev3.getMqttHelper().debug("Work has been aborted");
	            ev3.getMqttHelper().indicateTask(recipeID, "abort");
	        } catch (lejos.hardware.DeviceException de) {
	            de.printStackTrace();
	            ev3.setState(new Maint(),false);
	            ev3.getMqttHelper().debug("Problem Registering Ports on EV3");
	            ev3.getMqttHelper().indicateTask(recipeID, "abort");
	        } catch (NullPointerException ex) {
	            ex.printStackTrace();
	            ev3.setState(new Maint(),false);
	            ev3.getMqttHelper().debug("Problem Registering a specific Device on EV3");
	            ev3.getMqttHelper().indicateTask(recipeID, "abort");
	        } finally {
	            ev3.setWaiting(true);
	            ev3.getMqttHelper().debug("Closing Recipe");
	            recipe.close();
	        }
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
        ev3.setWaiting(false);
        
        if (!mqttConfirm) {
            ev3.setState(new ShuttingDown(),false);
            ev3.getMqttHelper().debug("Maximum Confirm waiting time passed");
        }
        
        ev3.getMqttHelper().debug("End of Proc");
    }
}
