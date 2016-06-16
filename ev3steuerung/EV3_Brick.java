package ev3steuerung;

import java.util.Map;
import java.util.HashMap;

import lejos.hardware.*;
import lejos.hardware.ev3.*;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.*;
import lejos.utility.Delay;

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
    private String nextRecipe;
    
    // Data Structures to save the needed Resources in
    protected String id;
    protected Map<Character,BaseRegulatedMotor> motorMap;
    protected Map<Integer,AnalogSensor> sensorMap;
    protected MqttHelper mqttHelper;
    // TODO: Insert protected Recipe Datastructure here
    
    // Internal EV3-Hardware
    private EV3 ev3;
    protected LED led;
    protected Audio audio;
    
    /**
     * Private Class Constructor that initializes the Hardware and sets
     * the first state to TurningOn
     */
    private EV3_Brick() {
        System.out.println("Hardware is being initialized"); //MQTT Message
        //initializeHardware();
        this.confirmed = false;
        this.fix = false;
        this.currentState = new TurningOn();
    }
    
    /**
     * Returns the Instance of the EV3_Brick if its already created, else
     * creates a new Instance and returns it [Singleton]
     * 
     * @return        EV3_Brick
     */
    protected static EV3_Brick getInstance() {
        if (instance == null)
            instance = new EV3_Brick();
        
        return instance;
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
    protected void setState(State s) {
        this.currentState = s;
    }
    
    /**
     * Check for a Manual Fix or other Fix for going out of MAINT
     * 
     * @return  boolean
     */
    protected boolean isFixed() {
        boolean result = this.fix;
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
        this.sleep = false;
        return result;
    }
    /**
     * Check for next Recipe (only after produce Indication from MES)
     * 
     * @return  String  Name of the Recipe to load up
     */
    protected String nextRecipe() {
        return this.nextRecipe;
    }
    
    /**
     * Initializes the Hardware
     */
    protected void initializeHardware() {
        this.ev3 = (EV3)BrickFinder.getDefault();
        audio = ev3.getAudio();
        led = ev3.getLED();
        
        identifyPorts();
        
        loadRecipes();
    }
    
    /**
     * Initialize Port-Settings (From Properties?)
     * and write them into the Instance Variables motor/sensor-Map
     * 
     * @return  boolean If this was successfuly or not
     */
    private boolean identifyPorts() {
        // MQTT DEBUG
        
        motorMap = new HashMap<Character,BaseRegulatedMotor>();
        sensorMap = new HashMap<Integer,AnalogSensor>();
        
        // Insert Identification Code from Sepp here
        
        return false;
    }
    
    /**
     * Loads up the nessecary Recipes
     * 
     * @return  boolean If this was successfuly or not
     */
    protected boolean loadRecipes() {
        // MQTT DEBUG
        // Insert Recipe-Loading Code here
        return true;
    }
    
    /**
     * Waits for specific Button Press (default any)
     * 
     * @param   but     Button to be pressed
     */
    protected void waitForButtonPress(String but) {
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
        Delay.msDelay(time);
    }
    
    /*
    *   MQTT Functions
    */
    /**
     * Starts Mqtt Handling
     */
    protected void startMqtt() throws InterruptedException {
        this.mqttHelper = new MqttHelper(this,"STP1001", "tcp://localhost", "192.168.1.1");
    }
    /**
     *  User pressed fix button in gui
     *  or this message arrived because of something else
     */
    protected void manualFix() {
        if (currentState instanceof Maint)
            this.fix = true;
    }
    /**
     *  User pressed shutdown button in gui
     *  or this message arrived because of something else
     */
    protected void emergencyShutdown() {
        System.out.println("emergency shutdown");
    }
    /**
     *  This always comes from topic vwp/toolid ???as mqtthelper handles everything else
     *  the messages are directly forwarded to this function, no checks are done
     * 
     *  @param message
     */
    protected void messageArrived(String message) {
        if (message.contains("produce") && currentState instanceof Idle) {
            this.produce = true;
            
            String recString = message.replaceAll("produce:", "");
            System.out.println("New Recipe = "+recString);
            
            this.nextRecipe = recString;
        } else {
            switch (message) {
                case "confirm":
                    this.confirmed = true;
                    break;
                case "sleep":
                    this.sleep = true;
                default:
                    System.out.println(message);
                    break;
            }
        }
    }
     /**
     * Stops Mqtt-Handling
     */
    protected void stopMqtt() {
        try {
            mqttHelper.discMqtt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mqttHelper.close();
    }
    
    protected void spinMotor(/*TODO: Insert Parameters here*/) {
        //TODO: new Spin(...);
        //TODO: OR Spin Logik hier rein ?
    }
}
