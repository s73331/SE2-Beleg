package ev3steuerung;

import java.util.HashMap;

/**
 * State Pattern Entity to Turn the Brick on and make everything functional
 * 
 * @see EV3_Brick
 * @author Christoph Schmidt
 * @version 0.9
 */
public class TurningOn implements State 
{
    public int getColor() {
        return 4; // Green flashing
    }
    
    public String getName() {
        return "TURNING_ON";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.getLED().setPattern(4);
        ev3.getMqttHelper().debug("Start of TurningOn");
        
        System.out.println("-> "+getName());
        ev3.getAudio().systemSound(2);
        
        ev3.setWaiting(true);
        ev3.getMqttHelper().debug("Waiting for confirmation of register");
        ev3.getMqttHelper().debug("Send Register to MES");
        ev3.getMqttHelper().register();
        
        boolean mqttConfirm = false;
        
        // WAIT FOR NON-CONFIRM
        for (int i = 0; i < ev3.REGCONF_TIMEOUT && !mqttConfirm; i++) {
        	ev3.getMqttHelper().debug("in For Schleife " + i);
            if (ev3.isConfirmed()) {
            	ev3.getMqttHelper().debug("Confirmed");
                mqttConfirm = true;
                ev3.setState(new Idle(),false);
                ev3.getMqttHelper().debug("Confirm acknowleged");
            } else {
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
        	ev3.getMqttHelper().debug("Maximum Confirm waiting time passed");
            ev3.setState(new ShuttingDown(),false);
        }
        
        //Import all correct recipes
        ev3.getMqttHelper().debug("try to load recipes from ./recipes");
        HashMap<String, Object> recipesMap = ev3.getJSONloader().loadRecipes();
        
        if(recipesMap.isEmpty() || recipesMap == null)
        { ev3.getMqttHelper().debug("No Recipes could be loaded");
          ev3.setState(new ShuttingDown(),false);
          
        }else{
        	ev3.setRecipes(recipesMap);
        }
        
        ev3.getMqttHelper().debug("End of TurningOn");
    }
}
