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
        initializeHardware();
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
        return false;
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
	*	MQTT Functions
	*/
    protected void startMqtt() throws InterruptedException {
        this.mqttHelper = new MqttHelper(this,"STP1001", "tcp://localhost", "192.168.1.1");
    }
    /**
     *	User pressed fix button in gui
	 *	or this message arrived because of something else
     */
    protected void manualFix() {
        System.out.println("manual fix");
    }
	/**
     *	User pressed shutdown button in gui
	 *	or this message arrived because of something else
     */
    protected void emergencyShutdown() {
        System.out.println("emergency shutdown");
    }
	/**
     *	This always comes from topic vwp/toolid ???as mqtthelper handles everything else
     *	the messages are directly forwarded to this function, no checks are done
     * 
	 *	@param message
     */
	protected void messageArrived(String message) {
		// TODO Auto-generated method stub
	}
    protected void stopMqtt() {
        mqttHelper.close();
    }
    
    protected void spinMotor(/*TODO: Insert Parameters here*/) {
        //TODO: new Spin(...);
        //TODO: OR Spin Logik hier rein ?
    }
}
