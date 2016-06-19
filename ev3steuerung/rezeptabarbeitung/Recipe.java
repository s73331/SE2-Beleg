package ev3steuerung.rezeptabarbeitung;

import java.util.Deque;
import java.util.LinkedList;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;

import ev3steuerung.EV3_Brick;

public class Recipe {
    
    //TODO Konstanten auslagern
    public static final boolean PARALLEL = true;
    public static final boolean SEQENZIELL = false;
    
    Deque<Object[]> rezept;
    Device[] devices;
    String name;
    EV3_Brick ev3;
    
    public Recipe(String recName, Deque<Object[]> rezept) {
        this.name = recName;
        this.rezept = rezept;
        this.ev3 = EV3_Brick.getInstance();
        ev3.mqttHelper.debug("Creating Recipe");
       }
    
    public void register() {
        ev3.mqttHelper.debug("Recipe: "+this+" registering Devices");
        /*Geräte registrieren*/
        this.devices = (Device[]) rezept.getFirst();
        
        for (Device x:devices){
            x.register();
        }   
        rezept.removeFirst();
        
       }
    
    public void work() {
        ev3.mqttHelper.debug("Recipe: "+this+" working the tasks");
        /* Rezepte ausführen*/
        while(!rezept.isEmpty()){ /* Solange Rezeptbefehle vorhanden sind*/
            
        Spin[] befehl = (Spin[]) rezept.getFirst();
        
        boolean mode;
        if(befehl.length == 1){ //mehrere Spins 
            mode = SEQENZIELL;
            }
        else
            mode = PARALLEL; // Auf Motoren warten bis nächsten Schritt
        
        for (Spin s:befehl){
            if(s.getTill() == 1){
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
        rezept.removeFirst(); /* Zuletzt ausgeführter Rezeptbefehl löschen*/
        }
    }
    
    public void close() {
        ev3.mqttHelper.debug("Recipe: "+this+" close Devices");
        /*Verbindungen zu Motoren/Sensoren trennen*/
        for (Device x:devices){
            x.close();
        }
        
        Button.waitForAnyPress();
    }
    
    public String toString() {
        return name;
    }
    
    public static Recipe load(String recName) {
        EV3_Brick.getInstance().mqttHelper.debug("Recipe.load( "+recName+" )");
        // GET ALL NAMES OF SUBDIRECTORY ./RECIPES
        
        /* Insert Code here */
        
        // Load Certain Recipe that is specified in the recName
        
        /* Insert Code here */
        
        // -> Sample Recipe here
        /* Sepp */
        
        LargeMotor d1 = new LargeMotor("B");
        MediumMotor d2 = new MediumMotor("A");
        TouchSensor d3 = new TouchSensor("S1");
        
        Device[] init = {d1, d2, d3};
        
        Spin s1 = new Spin(2500,360,0,0, 9);
        Spin s2 = new Spin(2500,360,1, 0,9);
        Spin s3 = new Spin(0,360, 1, 2, 1);
        
        
        Spin[] befehl4 ={s3}; 
        Spin[] befehl3 ={s1,s2}; 
        Spin[] befehl2 = {s1};
        
        Deque<Object[]> rezept = new LinkedList<>();
        
        rezept.addLast(init);
        rezept.addLast(befehl2);
        //rezept.addLast(befehl3);
        rezept.addLast(befehl4);
        
        /* Bis hier bekommen wir von Sepp übergeben */
        return new Recipe(recName, rezept);
    }
}
