package ev3steuerung; 

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import lejos.hardware.*;
import lejos.hardware.ev3.*;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.*;
import lejos.utility.Delay;
import ev3steuerung.rezeptabarbeitung.Recipe;

/**
 * EV3_Brick contains lightweight Hardware-Access and mimicks the Machine virtually
 * 
 * @author Christoph Schmidt
 * @version 0.8
 * @since 01.04.2016 */
 
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
    //protected Map<Character,BaseRegulatedMotor> motorMap;
    //protected Map<Integer,AnalogSensor> sensorMap;
    /** Variable to access mqtt-functionality of the program */
    private MqttHelper mqttHelper;
    /** Name of the next Recipe to load */
    protected String recName;
    /** Variable to see if the program is waiting for a response
     * to catch Out-Of-Time-Frame messages and discard them  */
    protected boolean waiting;
    
    // Internal EV3-Hardware
    private EV3 ev3;
    /** LED-Object of the EV3 */
    protected LED led;
    /** Audio-Object of the EV3 */
    protected Audio audio;
    
    // CONSTANTS
    /** Constant for MQTT-Handling */
    protected String DEVICE_ID, IP, MQTTSERV_IP;
    /** Constant for Time-Behaviour of the Program */
    protected int REGCONF_TIMEOUT, TASKCONF_TIMEOUT, TASKREQ_TIMEOUT, SLEEP_TIME, MAXMAINT_TIME;

    /* MAIN FUNCTIONS START */
    /*
     * Private Class Constructor that initializes the Hardware and sets
     * the first state to TurningOn */
    private EV3_Brick() {
        this.confirmed = false;
        this.fix = false;
        this.waiting = false;
        this.forcedState = false;
        
        // Load the Properties File "ev3steuerung.properties"
        initializeProperties();
        
        // Start MQTT-Handling
        try {
            startMqtt();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        // Initialize the LED / Audio / Maybe Ports as well TODO
        initializeHardware();
        
        // Set the State 
        this.setState(new TurningOn(), false) ;
    }
     
    /**
     * Returns the Instance of the EV3_Brick [Singleton]
     * 
     * @return  EV3_Brick - The Instance of the Singleton */
    public static EV3_Brick getInstance() {
        if (instance == null)
            instance = new EV3_Brick();
        return instance;
    }
    
    private void initializeProperties() {
        try {
            propertyHelper=new PropertyHelper("ev3steuerung.properties");
        } catch (IOException ioe) {
            System.out.println("The ev3steuerung.properties file could not be loaded");
            wait(5000);
            System.exit(1);
        }
        
        this.DEVICE_ID = propertyHelper.getName();
        this.IP = propertyHelper.getIP();
        this.MQTTSERV_IP = propertyHelper.getMqttServerIP();
        this.REGCONF_TIMEOUT = propertyHelper.getRegisterTimeout();
        this.TASKCONF_TIMEOUT = propertyHelper.getTaskIndConfirmTimeout();
        this.TASKREQ_TIMEOUT = propertyHelper.getTaskReqTimeout();
        this.SLEEP_TIME = propertyHelper.getSleepTime();
        this.MAXMAINT_TIME = propertyHelper.getMaxMaintTime();
        
        System.out.println("Properites loaded");
    }
    
    /*
     * Initializes the ev3-Object, the Audio-Object, the LED-Object and the Ports maybe TODO */
    private void initializeHardware() {
        mqttHelper.debug("Hardware is being initialized");
        this.ev3 = (EV3)BrickFinder.getDefault();
        audio = ev3.getAudio();
        led = ev3.getLED();
        
        identifyPorts();
    }
    
    /*
     * Initialize Port-Settings (From Properties?)
     * and write them into the Instance Variables motor/sensor-Map
     * 
     * @return  boolean If this was successfuly or not */
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
     * @return  State - Current Value of EV3_Brick.currentState
     * @see State */
    protected State getState() {
        return currentState;
    }
    
    /**
     * Changes the State to a given new state
     * 
     * @param  state New State to work next
     * @param  forced If the State shall be forced or not, default false.
     * If true, no change of state can occur after this. */
    protected void setState(State state, boolean forced) {
        mqttHelper.debug("Attempting to change State with Forced: "+forced);
        
        if (!forcedState) {
             mqttHelper.debug("Setting the State to: "+state.getName());
             this.currentState = state;
        } else
            mqttHelper.debug("Unable to set the State due to forcedState being true");
            
        if (forced) {
            forcedState = true;
            mqttHelper.debug("Forcing no other state changes");
        }
    }
    
    /**
     * Returns the current MqttHelper of the Machine
     * 
     * @return  MqttHelper - The main mqtt-sending Object
     * @see MqttHelper */
    public MqttHelper getMqttHelper() {
        return this.mqttHelper;
    }
    /*  MAIN FUNCTIONS END      */
    /*  WORKING FUNCTIONS START */
    
    /**
     * Check if a Manual fix message has arrived
     * @see Maint
     * @return  True - If there has been a "manual fix" message recieved */
    protected boolean isFixed() {
        boolean result = this.fix;
        mqttHelper.debug("Check Fix: "+result);
        if (result)
            this.fix = false;
        return result;
    }
    /**
     * Check if a Confirm message has arrived
     * @see Idle
     * @see Proc
     * @return  True - If there has been a "confirm" message recieved */
    protected boolean isConfirmed() {
        boolean result = this.confirmed;
        mqttHelper.debug("Check Confirm: "+result);
        if (result)
            this.confirmed = false;
        return result;
    }
    /**
     * Check if a produce message has arrived
     * @see Idle
     * @return  True - If there has been a "produce:taskXX" message recieved */
    protected boolean isProduce() {
        boolean result = this.produce;
        mqttHelper.debug("Check Produce: "+result);
        if (result)
            this.produce = false;
        return result;
    }
    /**
     * Check if a sleep message has arrived
     * @see Idle
     * @return  True - If there has been a "sleep" message recieved */
    protected boolean isSleep() {
        boolean result = this.sleep;
        mqttHelper.debug("Check Sleep: "+result);
        if (result)    
            this.sleep = false;
        return result;
    }
    /**
     * Starts the Loading of a certain Recipe
     * @see Proc
     * @see Recipe.load()
     * @param   recName The name of the Recipe to load up
     * @return  Recipe that shall be loaded */
    protected Recipe loadRecipe(String recName) {
        mqttHelper.debug("loadRecipe( "+recName+" )");
        /* Check if this works out with the ports , if not null*/
        
        /* Returns true if its ok, false if its not */
        return Recipe.load(recName);
    }
    
    /*  WORKING FUNCTIONS END   */
    /*  HELPER FUNCTIONS START  */
    
    /**
     * Waits for specific Button Press (default any)
     * 
     * @param   but String representation of Button to be pressed */
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
     * Waits a given time in ms
     * 
     * @param  time    time in ms
     */
    protected void wait(int time) {
        Delay.msDelay(time);
    }
    
    /*  HELPER FUNCTIONS END  */
    
    /*  MQTT FUNCTIONS START  */
    
    /**
     * Method to start MQTT-Handling by creating a
     * new MqttHelper Class and setting mqttHelper
     * 
     * @see MqttHelper
     * @throws InterruptedException When there was a problem creating the mqtt-handler */
    protected void startMqtt() throws InterruptedException {
        this.mqttHelper = new MqttHelper(this,DEVICE_ID, "tcp://"+MQTTSERV_IP, IP);
    }
    /**
     *  Method to call from MqttHelper when "manual fix" message arrives.
     *  This sets a fix flag, if the current State is instanceof Maint
     *  
     *  @see Maint
     *  @see MqttHelper */
    protected void manualFix() {
        mqttHelper.debug("Manual Fixing");
        if (currentState instanceof Maint)
            this.fix = true;
            mqttHelper.debug("Manual Fix Applied");
    }
    /**
     *  Method to call from MqttHelper when "emergency shutdown" message arrives.
     *  This sets the state forcibly to ShuttingDown
     *  
     *  @see ShuttingDown
     *  @see MqttHelper */
    protected void emergencyShutdown() {
        mqttHelper.debug("Emergency Shutdown");
        setState(new ShuttingDown(),true);
    }
    /**
     *  Method to call when a message is recieved on the Topic vwp/DEVICE_ID.
     *  No Checks have been made to the input.
     *  Handles produce, confirm, sleep
     *  
     *  @see MqttHelper
     *  @see Mqtt-Threads
     *  @param  message Message that arrived over Mqtt from MES */
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
     * Stops the Mqtt-Handler, closes all connections and deletes all folders
     * 
     * @see Control
     * @see MqttHelper.close() */
    protected void stopMqtt() {
        mqttHelper.debug("MQTT - Handler is stopping");
        mqttHelper.close();
    }
    
    /*  MQTT FUNCTIONS END  */
}