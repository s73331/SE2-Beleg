package ev3steuerung;

/**
 * Beschreiben Sie hier die Klasse TurnOn.
 * 
 * @author (Ihr Name) 
 * @version (eine Versionsnummer oder ein Datum)
 */
public class TurningOn implements State
{
    public TurningOn() {   };

    public int getColor() {
        return 4; // Green flashing
    }
    
    public String getName() {
        return "TURNING_ON";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.led.setPattern(4);
        ev3.mqttHelper.debug("Start of TurningOn");
        
        System.out.println("-> "+getName());
        ev3.audio.systemSound(3);
        
        ev3.mqttHelper.debug("Send Register to MES");
        ev3.mqttHelper.register();
        
        boolean mqttConfirm = false;
        
        // WAIT FOR NON-CONFIRM
        ev3.mqttHelper.debug("Waiting for confirmation of register");
        ev3.waiting = true;
        for (int i = 0; i < ev3.REGCONF_TIMEOUT && !mqttConfirm; i++) {
            if (ev3.isConfirmed()) {
                mqttConfirm = true;
                ev3.setState(new Idle(),false);
                ev3.mqttHelper.debug("Confirm acknowleged");
            } else {
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
        
        ev3.mqttHelper.debug("End of TurningOn");
    }
}
