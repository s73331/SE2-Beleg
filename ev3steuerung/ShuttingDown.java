package ev3steuerung;

/**
 * State Pattern Entity in MES-StateDiagram
 * 
 * @author Christoph Schmidt
 * @version 0.9
 * @see EV3_Brick
 * @see Maint
 */
public class ShuttingDown implements State
{
    public ShuttingDown() {   };

    public int getColor() {
        return 6; // Green flashing
    }
    
    public String getName() {
        return "DOWN";
    }
    
    public void doAction() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.getLED().setPattern(9);
        ev3.getMqttHelper().debug("Start of ShuttingDown");
        
        // MQTT STATE INDICATION
        ev3.getMqttHelper().indicateState(this.getName());
        ev3.getMqttHelper().publishState();
        System.out.println("-> "+getName());
        
        ev3.getMqttHelper().debug("State: "+getName());
        
        ev3.getAudio().systemSound(3);
        ev3.getLED().setPattern(0);
        ev3.getMqttHelper().debug("End of ShuttingDown");
    }
}
