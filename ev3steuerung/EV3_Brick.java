package ev3steuerung; 

import java.util.Map;
import java.util.HashMap;

import lejos.hardware.*;
import lejos.hardware.ev3.*;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.*;
import lejos.utility.Delay;
import ev3steuerung.rezeptabarbeitung.Recipe;

/**
 * EV3_Brick contains all Hardware-Access Points relevant to the Program
 * This is the main place you get your objects from, as its validated that these exist
 * 
 * @author Christoph Schmidt
 * @version 0.5
 */

public class EV3_Brick {
    // Instance of the Brick as its Singleton
    private static EV3_Brick instance;
    
    // State of the current Machine
    private State currentState;
    private boolean fix;
    private boolean confirmed;
    private boolean produce;
    private boolean sleep;
    private boolean forcedState;
    private PropertyHelper propertyHelper;
    
    // Data Structures to save the needed Resources in
    protected String deviceId;
    //protected Map<Character,BaseRegulatedMotor> motorMap;
    //protected Map<Integer,AnalogSensor> sensorMap;
    public MqttHelper mqttHelper;
    protected String recName;
    protected boolean waiting;
    // TODO: Insert protected Recipe Datastructure here
    
    // Internal EV3-Hardware
    private EV3 ev3;
    protected LED led;
    protected Audio audio;
    
    
    
    /* MAIN FUNCTIONS START */
    
    /**
     * Private Class Constructor that initializes the Hardware and sets
     * the first state to TurningOn
     */
    private EV3_Brick() {
        this.confirmed = false;
        this.fix = false;
        this.waiting = false;
        this.forcedState = false;
        try {
            // only for fix-Reasons with Monitoring-Tool!
            this.currentState = new Idle();
            startMqtt();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        //initializeProperties();
        //initializeHardware();
        
        this.currentState = new TurningOn();
    }
     
    /**
     * Returns the Instance of the EV3_Brick if its already created, else
     * creates a new Instance and returns it [Singleton]
     * 
     * @return        EV3_Brick
     */
    public static EV3_Brick getInstance() {
        if (instance == null)
            instance = new EV3_Brick();
        return instance;
    }
    
    private void initializeProperties() {
        mqttHelper.debug("initializeProperties()");
        try {
            propertyHelper=new PropertyHelper("ev3steuerung.properties");
        } catch (java.io.IOException ioe) {
            mqttHelper.debug("error: could not load properties\ncwd: "+System.getProperty("user.dir")+"\nexpected file: monitoringtool.properties\n"+ioe);
            mqttHelper.debug("initializeProperties(): \n"+ioe);
            System.exit(1);
        }
        
    }
    
    /**
     * Initializes the Hardware components
     */
    protected void initializeHardware() {
        mqttHelper.debug("Hardware is being initialized");
        this.ev3 = (EV3)BrickFinder.getDefault();
        audio = ev3.getAudio();
        led = ev3.getLED();
        
        identifyPorts();
    }
    
    /**
     * Initialize Port-Settings (From Properties?)
     * and write them into the Instance Variables motor/sensor-Map
     * 
     * @return  boolean If this was successfuly or not
     */
    private boolean identifyPorts() {
        mqttHelper.debug("Identify Ports");
        
        //motorMap = new HashMap<Character,BaseRegulatedMotor>();
        //sensorMap = new HashMap<Integer,AnalogSensor>();
        
        // Insert Identification Code from Sepp here
        
        return false;
    }
    
    /**
     * Returns the current State of the Machine
     * 
     * @return        State
     */
    protected State getState() {
        return currentState;
    }
    
    /**
     * Changes the State to a given new state
     * 
     * @param  s    The new State for changing into
     */
    protected void setState(State s, boolean forced) {
        mqttHelper.debug("Attempting to change State with Forced: "+forced);
        
        if (!forcedState) {
             mqttHelper.debug("Setting the State to: "+s.getName());
             this.currentState = s;
        } else
            mqttHelper.debug("Unable to set the State due to forcedState being true");
            
        if (forced) {
            forcedState = true;
            mqttHelper.debug("Forcing no other state changes");
        }
    }
    
    /* MAIN FUNCTIONS END */
    
    
    
    /**
     * Check for a Manual Fix or other Fix for going out of MAINT
     * 
     * @return  boolean
     */
    protected boolean isFixed() {
        boolean result = this.fix;
        mqttHelper.debug("Check Fix: "+result);
        if (result)
            this.fix = false;
        return result;
    }
    /**
     * Check for a confirmed Message from MES
     * 
     * @return  boolean
     */
    protected boolean isConfirmed() {
        boolean result = this.confirmed;
        mqttHelper.debug("Check Confirm: "+result);
        if (result)
            this.confirmed = false;
        return result;
    }
    /**
     * Check for a Produce Message from MES
     * 
     * @return  boolean
     */
    protected boolean isProduce() {
        boolean result = this.produce;
        mqttHelper.debug("Check Produce: "+result);
        if (result)
            this.produce = false;
        return result;
    }
    /**
     * Check for a Sleep Message from MES
     * 
     * @return  boolean
     */
    protected boolean isSleep() {
        boolean result = this.sleep;
        mqttHelper.debug("Check Sleep: "+result);
        if (result)    
            this.sleep = false;
        return result;
    }
    
    /**
     * Loads up the nessecary Recipe
     * 
     * @return  boolean If this was successfuly or not
     */
    protected Recipe loadRecipe(String recName) {
        mqttHelper.debug("loadRecipe( "+recName+" )");
        
        /* Check if this works out with the ports , if not null*/
        
        /* Returns true if its ok, false if its not */
        return Recipe.load(recName);
    }
    
    /**
     * Waits for specific Button Press (default any)
     * 
     * @param   but     Button to be pressed
     */
    protected void waitForButtonPress(String but) {
        mqttHelper.debug("Wait for button press "+but);
        switch (but) {
            default:
            case "any":
                Button.waitForAnyPress();
                break;
            //TODO: More Cases with other buttons
        }
    }
    
    /**
     * Returns the Audio-Object of the EV3
     * @return        Audio
     */
    // protected Audio getAudio() {
    //    return audio;
    //}
    
    /**
     * Returns the LED-Object of the EV3
     * @return        LED
     */
    //protected LED getLED() {
    //    return led;
    //}
    
    /**
     * Waits a given time in ms
     * 
     * @param  time    time in ms
     */
    protected void wait(int time) {
        mqttHelper.debug("Waiting "+time+" ms");
        Delay.msDelay(time);
    }
    
    /*
    *   MQTT Functions
    */
    /**
     * Starts Mqtt Handling
     */
    protected void startMqtt() throws InterruptedException {
        this.mqttHelper = new MqttHelper(this,deviceId, "tcp://localhost", "192.168.1.1");  //CHRISTOPH: statt tcp://localhost die mqttserverip
    }
    /**
     *  User pressed fix button in gui
     *  or this message arrived because of something else
     */
    protected void manualFix() {
        mqttHelper.debug("Manual Fixing");
        if (currentState instanceof Maint)
            this.fix = true;
    }
    /**
     *  User pressed shutdown button in gui
     *  or this message arrived because of something else
     */
    protected void emergencyShutdown() {
        mqttHelper.debug("Emergency Shutdown");
        setState(new ShuttingDown(),true);
    }
    /**
     *  This always comes from topic vwp/toolid ???as mqtthelper handles everything else
     *  the messages are directly forwarded to this function, no checks are done
     * 
     *  @param  String  message that arrived
     */
    protected synchronized void messageArrived(String message) {
        if (message.contains("produce") && currentState instanceof Idle && waiting && !produce) {
            this.produce = true;
            
            String recString = message.replaceAll("produce:", "");
            mqttHelper.debug("Message Arrived: produce:"+recString);
            this.recName = recString;
            
        } else if (waiting) {
            switch (message) {
                case "confirm":
                    mqttHelper.debug("Message Arrived: "+message);
                    this.confirmed = true;
                    break;
                case "sleep":
                    mqttHelper.debug("Message Arrived: "+message);
                    this.sleep = true;
                    break;
                default:
                    mqttHelper.debug("unknown message arrived: "+message);
                    break;
            }
        } else {
            mqttHelper.debug("Out of Time-Frame-Message recieved: "+message);
        }
    }
     /**
     * Stops Mqtt-Handling
     */
    protected void stopMqtt() {
        mqttHelper.debug("MQTT - Handler is stopping");
        mqttHelper.close();
    }
}