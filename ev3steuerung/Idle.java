package ev3steuerung;

/**
 * State Pattern Entity in MES-StateDiagram
 * 
 * @author Christoph Schmidt
 * @version 0.9
 * @see TurningOn
 * @see Proc
 * @see Maint
 */
public class Idle implements State
{
    public int getColor() {
        return 3; // Green flashing
    }
    
    public String getName() {
        return "IDLE";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.getLED().setPattern(3);
        ev3.getMqttHelper().debug("Start of Idle");
        
        // MQTT STATE INDICATION
        ev3.getMqttHelper().indicateState(this.getName());
        ev3.getMqttHelper().publishState();
        System.out.println("-> "+getName());
        
        // //ev3.getAudio().systemSound(0);
        //ev3.wait(4000);
        
        boolean produce = false;
        
        // WAIT FOR NON-CONFIRM
        ev3.getMqttHelper().debug("Waiting for produce-task or sleep");
        ev3.setWaiting(true);
        for (int i = 0; i < ev3.TASKREQ_TIMEOUT && !produce; i++) {
            if (ev3.isSleep()) {
                i = 0;
                ev3.getMqttHelper().debug("Sleep acknowlegded, Resetting Wait time");
            }
            if (ev3.isProduce()) {
                produce = true;
                ev3.setState(new Proc(),false); //TODO: RECIPE STRUCTURES
                ev3.getMqttHelper().debug("Produce acknowlegded");
            }
            else {
                ev3.getMqttHelper().requestTask();
                try {
                    ev3.getMqttHelper().debug("Waiting longer for produce or sleep");
                    Thread.sleep(1000*ev3.SLEEP_TIME);
                } catch (InterruptedException ie) {
                    ev3.getMqttHelper().debug("sleep/produce Wait InterruptedException recieved");
                    ie.printStackTrace();
                }
            }
        }
        ev3.setWaiting(false);
        
        // If There was neither a Sleep, nor a produce or the Time has run out
        if (!produce) {
            ev3.setState(new Maint(),false);
            ev3.getMqttHelper().debug("Maximum produce or sleep wait time has passed");
        }
        
        ev3.getMqttHelper().debug("End of Idle");
    }
}
