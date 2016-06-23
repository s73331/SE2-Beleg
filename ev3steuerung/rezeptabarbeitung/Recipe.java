package ev3steuerung.rezeptabarbeitung;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.utility.Delay;


import ev3steuerung.EV3_Brick;

public class Recipe {
    
    /** Constant for the Work on the Recipe */
    public static final boolean PARALLEL = true;
    public static final boolean SEQENZIELL = false;
    public static final int TOUCHWAITTIMEOUT = 10000;
    
    private Deque<Object[]> rezept;
    private Device[] devices;
    private String name;
    private EV3_Brick ev3;
    
    private Recipe(String recName, Deque<Object[]> rezept) {
        this.name = recName;
        this.rezept = rezept;
        this.ev3 = EV3_Brick.getInstance();
        ev3.getMqttHelper().debug("Creating Recipe");
    }
    
    /**
     * Registers all Devices that are used from the Recipe
     * 
     * @throws lejos.hardware.DeviceException - When a Device is unable to open its port
     * @see Device
     * */
    public void register() throws lejos.hardware.DeviceException, NullPointerException {
        ev3.getMqttHelper().debug("Recipe: "+this+" registering Devices");
        /* Ger�te registrieren */
        this.devices = (Device[]) rezept.getFirst();
        for (Device x:devices){
            if (!x.register()) {
                throw new NullPointerException("Failed Registering a Device");
            }
        }
        rezept.removeFirst();
    }
    
    /**
     * Close the Devices and disconnect the ports that are used in the Recipe
     * 
     * @see Device
     */
    public void close() {
        ev3.getMqttHelper().debug("Recipe: "+this+" close Devices");
        /* Verbindungen zu Motoren/Sensoren trennen */
        for (Device x:devices){
            x.close();
        }
    }
    
    /**
     * Method to start the work on the Recipe.
     * 
     * @throws InterruptedException - Is thrown when someone intervenes
     * or the Timeout has been reached
     * @see Device
     * @return True - When The whole Recipe has been finished without Problems
     */
    public boolean work() throws InterruptedException {
        ev3.getMqttHelper().debug("Recipe: "+this+" working the tasks");
        boolean ok = true;
        
        /* Solange Rezeptbefehle vorhanden sind*/
        while(!rezept.isEmpty() && ok) { 
            
            boolean pressinTime;
            boolean releaseinTime;
            boolean rightAngle;
            boolean run = true;
            int time = 0;
            boolean stall = false;
            
            /* Spin starten */
            if(rezept.getFirst().getClass().toString().contains("Spin")) {
                Spin[] befehl = (Spin[]) rezept.getFirst();
                boolean mode;
                if(befehl.length == 1) { //mehrere Spins 
                    mode = SEQENZIELL;
                    }
                else
                    mode = PARALLEL; // Auf Motoren warten bis nächsten Schritt
                
                for (Spin s:befehl) {
                    ev3.getMqttHelper().debug("Working on Spin "+s.toString());
                    
                    BaseRegulatedMotor em = ((MotorDevice)devices[s.getDevice()]).getEV3Motor();
                    TouchSensor touch = (TouchSensor)devices[s.getSensor()];
                    MotorDevice md = (MotorDevice)devices[s.getDevice()];
                    em.setStallThreshold(2, 50);
                    if(s.getTill() == 1) {
                        System.out.println("Press TouchSensor");
                        ev3.getMqttHelper().debug("Wait util its pressed");
                        md.forward(s.getSpeed());
                        stall = false;
                        time = 0;
                        while (!touch.isPressed() && time < TOUCHWAITTIMEOUT && !stall) {
                            if (em.isStalled()) {
                                ev3.getMqttHelper().debug("Stalling detected");
                                stall = true;
                            }
                            Delay.msDelay(50);
                            time += 50;
                        }
                        
                        if (stall || time >= TOUCHWAITTIMEOUT) {
                                md.stop();
                                ok = false;
                                throw new InterruptedException("Stalled or Timeout");
                        }
                        md.stop();
                    }
                    else if(s.getTill() == 0){
                        System.out.println("Release TouchSensor");
                        ev3.getMqttHelper().debug("Wait until its released");
                        md.forward(s.getSpeed());
                        time = 0;
                        stall = false;
                        while (touch.isPressed() && time < TOUCHWAITTIMEOUT && !stall) {
                            if (em.isStalled()) {
                                ev3.getMqttHelper().debug("Stalling detected");
                                stall = true;
                            }
                            Delay.msDelay(50);
                            time += 50;
                        }
                        
                        if (stall || time >= TOUCHWAITTIMEOUT) {
                                md.stop();
                                ok = false;
                                throw new InterruptedException("Stalled or Timeout");
                        }
                    }
                    else if(s.getTill() == 9){
                        ev3.getMqttHelper().debug("Turn to an angle");
                        md.rotate(mode, s.getSpeed(), s.getAngle());
                        time = 0;
                        while (time < TOUCHWAITTIMEOUT && !stall && md.getEV3Motor().isMoving()) {
                            if (em.isStalled()) {
                                ev3.getMqttHelper().debug("Stalling detected");
                                stall = true;
                            }
                            Delay.msDelay(50);
                            time += 50;
                        }
                        if (time >= TOUCHWAITTIMEOUT || stall) {
                            md.stop();
                            throw new InterruptedException("not the right angle!");
                        }
                    }
                }
            }
            /* Wartezeit starten */
            else if(rezept.getFirst().getClass().toString().contains("Wait")) {
                ev3.getMqttHelper().debug("Waiting operation");
                Wait[] befehl = (Wait[]) rezept.getFirst();
                TouchSensor touch = (TouchSensor)devices[befehl[0].getSensor()];
                time = 0;
                
                switch(befehl[0].getMode()) {
                case 0:
                    long toWait = befehl[0].getMs();
                    ev3.getMqttHelper().debug("Wait certain time");
                    Delay.msDelay(toWait);
                    break;
                    
                case 1:
                    System.out.println("Release TouchSensor");
                    ev3.getMqttHelper().debug("Wait for Release");
                    time = 0;
                    
                    while (touch.isPressed() && time < TOUCHWAITTIMEOUT) {
                        Delay.msDelay(50);
                        time += 50;
                    }
                    ev3.getMqttHelper().debug("Done waiting time: "+time);
                    if (time >= TOUCHWAITTIMEOUT) {
                        ok = false;
                        ev3.getMqttHelper().debug("The wait timed out");
                        throw new InterruptedException("Timeout waiting");
                    }
                    break;
                
                case 2:
                    System.out.println("Press TouchSensor");
                    ev3.getMqttHelper().debug("Wait for Press");
                    time = 0;
                    
                    while (!touch.isPressed() && time < TOUCHWAITTIMEOUT ) {
                        Delay.msDelay(50);
                        time += 50;
                    }
                    
                    if (time >= TOUCHWAITTIMEOUT) {
                        ok = false;
                        ev3.getMqttHelper().debug("The wait timed out");
                        throw new InterruptedException("Timeout waiting");
                    }
                    break;
                
                }
            }
            rezept.removeFirst(); /* Zuletzt ausgeführter Rezeptbefehl löschen*/
        }
        
        if (rezept.isEmpty())
            return true;
        else
            return false;
    }
    
    /**
     * Loads up the specified Recipe, creates a new Recipe-Object,
     * loads the Resources and returns it.
     * 
     * @param recName Name of the Recipe that shall be loaded
     * @Return Recipe that was created and filled
     * @see EV3_Brick.loadRecipe(..)
     */
    public static Recipe load(String recName) {
        // GET ALL NAMES OF SUBDIRECTORY ./RECIPES
        
        /* Insert Code here start */
        
        // Load Certain Recipe that is specified in the recName
        
        /* Insert Code here end */
        
        // -> Sample Recipe here
        /* Sepp */
        
        LargeMotor d1 = new LargeMotor("B");
        MediumMotor d2 = new MediumMotor("A");
        TouchSensor d3 = new TouchSensor("S2");
        ColorSensor d4 = new ColorSensor("S1");

        
        Device[] init = {d1, d2, d3, d4};
        
        Spin s1 = new Spin(2500,360,1,0, 9);    // Medium Motor allein bis Gradzahl
        Spin s2 = new Spin(2500,360,0, 0,9);    // Große Motor allein bis Gradzahl
        Spin s3 = new Spin(0,360, 0, 2, 1);     // Drehe bis Sensor gedrückt wird
        Spin s4 = new Spin(0, 360, 0, 2, 0);    // Drehe bis Sensor losgelassen
        Wait s5 = new Wait(5000, 0);            // Warten in ms
        Wait s6 = new Wait(2, 1);               // Warten auf Touch
        
        
        /*angle, speed, welcher Motor ,welcher Touchsensor, Touchsensor Einstellung*/
        
        Wait[] befehl8 = {s6};
        Wait[] befehl7 = {s5};
        Spin[] befehl6 = {s2};
        Spin[] befehl5 = {s4};
        Spin[] befehl4 ={s3};
        Spin[] befehl3 ={s1,s2};
        Spin[] befehl2 = {s1};
    
        Deque<Object[]> rezept = new LinkedList<>();
        
        rezept.addLast(init);
        //rezept.addLast(befehl6);
        //rezept.addLast(befehl3);
        rezept.addLast(befehl4);
        //rezept.addLast(befehl5);
        //rezept.addLast(befehl7);  // Color
        
        //rezept.addLast(befehl2);
        //rezept.addLast(befehl8);
        //rezept.addLast(befehl2);


        /* Bis hier bekommen wir von Sepp übergeben */
        return new Recipe(recName, rezept);
    }
    
    
    /**
     * Override the toString()-Method to display the name of the Recipe
     * 
     * @return Name of the Recipe
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Overrides the usual Hashcode-method to return
     * the name as HashCode for Identification purpose in a HashSet
     * 
     * @return hashcode that is used to identify it
     * @see EV3_Brick
     * @see java.util.HashSet
     */
    @Override
    public int hashCode() {
        int i = 0;
        int num = 0;
        boolean isNeg = false;
        
        //Process each character of the string;
        while( i < name.length()) {
            num *= 10;
            num += name.charAt(i++) - '0'; //Minus the ASCII code of '0' to get the value of the charAt(i++).
        }
        
        //Check for negative sign; if it's there, set the isNeg flag
        if (name.charAt(0) == '-') {
            isNeg = true;
            i = 1;
        }
        
        // Check for negativity, if so make it positive again
        if (isNeg)
            num = -num;
            
        return num;
    }
    
    /**
     * Overrides the equals-Method to better check if two
     * Recipes are in fact equal
     * 
     * @param obj The Object to check equality against
     * @return True - If the Objects describe the same Recipe
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Recipe other = (Recipe) obj;
      if (!this.toString().equals(other.toString()))
         return false;
      return true;
    }
}
