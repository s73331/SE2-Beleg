package ev3steuerung;

/**
 * State Pattern Entity in MES-StateDiagram
 * 
 * @author Christoph Schmidt
 * @version 0.9
 * 
 * @see Idle
 * @see Proc
 * @see ShuttingDown
 */
public class Maint implements State
{
    public Maint() {   };

    public int getColor() {
        return 5; // Green flashing
    }
    
    public String getName() {
        return "MAINT";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.led.setPattern(5);
        ev3.getMqttHelper().debug("Start of Maint");
        
        // MQTT STATE INDICATION
        ev3.getMqttHelper().indicateState(this.getName());
        ev3.getMqttHelper().publishState();
        System.out.println("-> "+getName());
        
        System.out.println("Fix the Machine");
        
        boolean errorMode = true;
        ev3.getMqttHelper().debug("Waiting for manual fix");
        for (int i = 0; i < ev3.MAXMAINT_TIME && errorMode; i++) {
            if (ev3.isFixed()) {
                ev3.getMqttHelper().debug("Manual Fix acknowledged");
                errorMode = false;
            } else {
                try {
                    ev3.getMqttHelper().debug("Waiting longer for manual fix");
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ev3.getMqttHelper().debug("Interrupted Exception recieved");
                    ie.printStackTrace();
                }
            }
        }
        
        if (!errorMode) {
            ev3.getMqttHelper().debug("Manual Fix applied");
            ev3.setState(new Idle(),false);
        } else {
            ev3.getMqttHelper().debug("Maximum fix wait time has passed");
            ev3.setState(new ShuttingDown(),false);
        }
        
        ev3.getMqttHelper().debug("End of Maint");
    }
}
