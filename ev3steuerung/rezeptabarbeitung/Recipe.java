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
    public void register() throws lejos.hardware.DeviceException {
        ev3.getMqttHelper().debug("Recipe: "+this+" registering Devices");
        /*Geräte registrieren*/
        this.devices = (Device[]) rezept.getFirst();
        
        for (Device x:devices){
            x.register();
        }   
        rezept.removeFirst();
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
        
        /* Rezepte ausführen*/
        while(!rezept.isEmpty() && ok) { /* Solange Rezeptbefehle vorhanden sind*/
            
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
                    if(s.getTill() == 1) {
                        devices[s.getDevice()].forward(s.getSpeed());
                        devices[s.getSensor()].waitForPress();
                        devices[s.getDevice()].stop();
                    }
                    else if(s.getTill() == 0){
                        devices[s.getDevice()].forward(s.getSpeed());
                        devices[s.getSensor()].waitForRelease();
                        devices[s.getDevice()].stop();
                    }
                    else if(s.getTill() == 9){
                        devices[s.getDevice()].rotate(mode, s.getSpeed(), s.getAngle());
                    }
                }
                
            }
            /* Wartezeit starten */
            else if(rezept.getFirst().getClass().toString().contains("Wait")) {
                Wait[] befehl = (Wait[]) rezept.getFirst();
                    
                
                switch(befehl[0].getMode()) {
                case 0:
                    befehl[0].waitTime();
                    break;
                    
                case 1:
                    devices[befehl[0].getSensor()].waitForPress();
                    break;
                
                case 2:
                    devices[befehl[0].getSensor()].waitForRelease();
                    break;
                
                }
            }
        rezept.removeFirst(); /* Zuletzt ausgeführter Rezeptbefehl löschen*/
        }
        return true;
    }
    
    public void close() {
        ev3.mqttHelper.debug("Recipe: "+this+" close Devices");
        /*Verbindungen zu Motoren/Sensoren trennen*/
        for (Device x:devices){
            x.close();
        }
    }
    
    public String toString() {
        return name;
    }
    
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
}
