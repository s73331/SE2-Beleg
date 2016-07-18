package ev3steuerung; 

import java.util.ArrayDeque;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import lejos.hardware.*;
import lejos.hardware.ev3.*;
import lejos.hardware.port.Port;
import ev3steuerung.rezeptabarbeitung.Recipe;

/**
 * EV3_Brick contains lightweight Hardware-Access and mimicks the Machine virtually
 * 
 * @author Christoph Schmidt
 * @version 0.8
 * @since 01.04.2016 */
 
public class EV3_Brick implements MqttBrick {
    // Instance of the Brick as its Singleton
    private static EV3_Brick instance;
    
    // State of the current Machine
    private State currentState;
    private boolean fix;
    private boolean confirmed;
    private boolean produce;
    private boolean sleep;
    private boolean forcedState;
    private String recipeID;
    private PropertyLoader propertyHelper;
    private MqttHelper mqttHelper;
    private JSONloader jsonloader;
    private HashMap<String, Object> recipesMap;
    
    /** Variable to see if the program is waiting for a response
     * to catch Out-Of-Time-Frame messages and discard them  */
    private boolean waiting;
    /** Lejos-EV3-Object from the machine*/
    private EV3 ev3;
    /** LED-Object of the EV3 */
    private LED led;
    /** Audio-Object of the EV3 */
    private Audio audio;
    
    // CONSTANTS
    /** Constant for MQTT-Handling */
    protected String DEVICE_ID, IP, MQTTSERV_IP;
    /** Constant for Time-Behaviour of the Program */
    protected int REGCONF_TIMEOUT, TASKCONF_TIMEOUT, TASKREQ_TIMEOUT, SLEEP_TIME, MAXMAINT_TIME;

    /* MAIN FUNCTIONS START */
    /**
     * Private Class Constructor that initializes the Hardware and sets
     * the first state to TurningOn */
    private EV3_Brick() {
        this.confirmed = false;
        this.fix = false;
        this.waiting = false;
        this.forcedState = false;
    }
    
    /**
     * Returns the Instance of the EV3_Brick [Singleton]
     * 
     * @return  EV3_Brick - The Instance of the Singleton */
    public static EV3_Brick getInstance() {
        if (instance == null)
        {
            instance = new EV3_Brick();
            instance.startEV3();
        }
        return instance;
    }
    
    /**
     * Startup Method called by Constructor. Starts PropertyLoad, Mqtt and initializes the Hardware
     */
    private void startEV3() {
        // Load the Properties File "ev3steuerung.properties"
        initializeProperties();
        
        // Start MQTT-Handling
        
        System.out.println("Connecting to MQTT-Broker");
        try {
            startMqtt();
        } catch (Exception ie) {
            System.out.println("Mqtt-Handler could not be started");
            ie.printStackTrace();
            System.exit(0);
        }
        
        // Initialize the LED / Audio / Recipe-set
        initializeHardware();
        
        //create JSONloader object
        initializeJSONloader();
        
        // Set the State 
        this.setState(new TurningOn(), false);
    }
    
    private void initializeProperties() {
        try {
            propertyHelper=new ConcretePropertyLoader("ev3steuerung.properties");
        } catch (IOException ioe) {
            System.out.println("The ev3steuerung.properties file could not be loaded");
            System.exit(0);
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
        try {
            this.ev3 = (EV3)BrickFinder.getLocal();//getDefault();
            audio = ev3.getAudio();
            led = ev3.getLED();
        } catch (Exception e) {
            System.out.println("The Brick could not be loaded");
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    /**
     * Returns the current State of the Machine
     * 
     * @return  State - Current Value of EV3_Brick.currentState
     * @see State */
    public State getState() {
        return this.currentState;
    }
    
    public Port getPortfromString(String p) {
    	return ev3.getPort(p);
    }
    
    /**
     * Returns the Name of the current State
     * 
     * @return The Name of the current State
     */
    public String getStateName() {
        return this.currentState.getName();
    }
    
    /**
     * Returns the Audi-Object of the EV3 for Soundeffects of the State
     * 
     * @return audio - The lejos.hardware.Audio Object
     * @see State
     */
    public Audio getAudio() {
        return this.audio;
    }
    
    /**
     * Returns the LED-Object of the EV3 for Visual Apperance of the State
     * 
     * @return led - The lejos.hardware.LED Object
     */
    public LED getLED() {
        return this.led;
    }
    
    /**
     * Setter for Waiting Variable [TimeWindow]-For Message-Input
     * When True, then a Message is awaited
     * When False, then no Message is awaited
     * 
     * @param wait The Boolean value waiting shall be set to
     * @see Proc
     * @see Idle
     * @see MqttBrick.messageArrived
     * 
     */
    public void setWaiting(boolean wait) {
        this.waiting = wait;
    }
    
    public void setRecipes(HashMap recipes) {
    	this.recipesMap = recipes;
    }
    
    public ArrayDeque<Object[]> getRecipe(String recipeID){
    	
    	ArrayDeque<Object[]> deque =(ArrayDeque<Object[]>) this.recipesMap.get(recipeID+"_deque");
    	if(deque != null){
    		//Rezept schon geladen
    		//check for Update
    		
    		File recipePath = new File("./recipes/" + this.recipesMap.get(recipeID+"_filename"));
    		
    		if(this.recipesMap.get(recipeID).equals(jsonloader.getHash(recipePath)))
    		{   
    			mqttHelper.debug("same Hash, use cached recipe");
    			System.out.println("use cached recipe");
    			return (ArrayDeque<Object[]>) this.recipesMap.get(recipeID+"_deque");
    		}
            else {
            	mqttHelper.debug("changed Hash, load file new");

                // alten einträge löschen
                recipesMap.remove(recipeID);
                recipesMap.remove(recipeID +"_deque");
                recipesMap.remove(recipeID +"_filename");
                
                mqttHelper.debug("delete old keys in our HashMap");

                //versuchen verändertes Rezept neu einzulesen
                if(jsonloader.buildDeuque(recipePath,recipesMap))
                {
                	mqttHelper.debug("successfully read new recipe");
                	Object newRecipedeque = this.recipesMap.get(recipeID+"_deque");
                	
                	if( newRecipedeque != null)
                	{
                		System.out.println("sucessfully load updated recipe");
                	return (ArrayDeque<Object[]>) this.recipesMap.get(recipeID+"_deque");
                	}else
                	{//wenn im rezept die ID verändert wurde landet man hier
                		return null;}
 
                }else{
                	return null;
                }
    		
    	}
    	}else{
    		//Rezept noch nicht vorhanden -> Alle neu laden
    		//TODO
    		//einfache Lösung nichts machen
    		//-> bedeutet neue Rezepte werden nur bei neustart eingelesen
    		return null;
    	}
    	
    }
    /**
     * Changes the State to a given new state
     * 
     * @param  state New State to work next
     * @param  forced If the State shall be forced or not, default false.
     * If true, no change of state can occur after this. */
    public void setState(State state, boolean forced) {
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
    
    public JSONloader getJSONloader() {
    	return this.jsonloader;
    }
    
    /**
     * Returns the next Recipe that shall be completed
     * 
     * @return Recipe - Object that was loaded
     */
    public String getrecipeID() {
        return this.recipeID;
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
    
    /*  WORKING FUNCTIONS END   */
    /*  HELPER FUNCTIONS START  */
    
    /*
     * Waits for specific Button Press (default any)
     * 
     * @param   but String representation of Button to be pressed 
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
     *
    protected void wait(int time) {
        Delay.msDelay(time);
    }*/
    
    /*  HELPER FUNCTIONS END  */
    
    /*  MQTT FUNCTIONS START  */
    
    /**
     * Method to start MQTT-Handling by creating a
     * new MqttHelper Class and setting mqttHelper
     * 
     * @see MqttHelper
     * @throws InterruptedException When there was a problem creating the mqtt-handler */
    public void startMqtt() throws InterruptedException {
        this.mqttHelper = new MqttHelper(this,DEVICE_ID, "tcp://"+MQTTSERV_IP, IP);
    }
    
    public void initializeJSONloader() {
    	mqttHelper.debug("JSONloader is being initialized");
    	this.jsonloader = new JSONloader();
    }
    /**
     *  Method to call from MqttHelper when "manual fix" message arrives.
     *  This sets a fix flag, if the current State is instanceof Maint
     *  
     *  @see Maint
     *  @see MqttHelper */
    public void manualFix() {
        mqttHelper.debug("Manual Fixing");
        if (getStateName().equals("MAINT") || getStateName().equals("DOWN")) {
            this.fix = true;
            mqttHelper.debug("Manual Fix Applied");
        }
    }
    /**
     *  Method to call from MqttHelper when "emergency shutdown" message arrives.
     *  This sets the state forcibly to ShuttingDown
     *  
     *  @see ShuttingDown
     *  @see MqttHelper */
    public void emergencyShutdown() {
        mqttHelper.debug("Emergency Shutdown");
        setState(new ShuttingDown(),true);
    }
    /**
     *  Method to call when a message is recieved on the Topic vwp/DEVICE_ID.
     *  No Checks have been made to the input.
     *  Handles produce, confirm, sleep
     *  
     *  @param  message Message that arrived over Mqtt from MES 
     *  @see MqttHelper
     *  @see Mqtt-Threads 
     *  @see Recipe
     */
    public synchronized void messageArrived(String message) {
    	
    	
    	
        if (message.contains("produce") && getStateName().equals("IDLE") && waiting && !produce) {
            mqttHelper.debug("Message Arrived: "+message);
            String recString;
            
            // Add the recipe name to the variable recipeID
            recString = message.replaceAll("produce:", "");     // Remove Produce:
            recString = recString.replaceAll(" ", "");            // Remove Whitespaces
            
            if (recString.isEmpty()) {
                mqttHelper.debug("Recipe-String was empty, did not recieve a valid response");
                return;
            }
            
            this.recipeID = recString;
            this.produce = true;
                
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
                    mqttHelper.debug("Unknown Message Arrived: "+message);
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
    public void stopMqtt() {
        mqttHelper.debug("MQTT - Handler is stopping");
        mqttHelper.close();
    }
    /*  MQTT FUNCTIONS END  */
}