package ev3steuerung.rezeptabarbeitung;


import java.util.ArrayDeque;
import lejos.hardware.device.DeviceIdentifier;
import lejos.hardware.motor.BaseRegulatedMotor;
import lejos.utility.Delay;



import ev3steuerung.EV3_Brick;

public class Recipe {
    
    /** Constant for the Work on the Recipe */
    public static final boolean PARALLEL = true;
    public static final boolean SEQENZIELL = false;
    public static final int TOUCHWAITTIMEOUT = 50000;
    
    private ArrayDeque<Object[]> rezept;
    private Device[] devices;
    private EV3_Brick ev3;
    private String recipeID;
    
    public Recipe(ArrayDeque<Object[]> rezept, String recipeID) {
        this.rezept = rezept;
        this.ev3 = EV3_Brick.getInstance();
        this.recipeID = recipeID;
        ev3.getMqttHelper().debug("Creating Recipe");
    }
    
    
    public boolean checkConfiguration(){

        this.devices = (Device[]) rezept.getFirst();
        String[] Ports = {"A", "B", "C", "D", "S1", "S2", "S3", "S4"};
        
        
    	for (int i = 0; i<8 ; i++){
            if(devices[i] != null){
        		DeviceIdentifier ids = new DeviceIdentifier(ev3.getPortfromString(Ports[i]));
        		String signature = ids.getDeviceSignature(false);
            	switch (devices[i].getClass().getName()) {
            	case "ev3steuerung.rezeptabarbeitung.MediumMotor" :	
            		if ( !signature.contains("OUTPUT_TACHO:MINITACHO") )
            		{
            			ev3.getMqttHelper().debug("No MediumMotor on Port " +Ports[i]);
            			ids.close();
            			return false;
            		}
            		break;
            	case "ev3steuerung.rezeptabarbeitung.LargeMotor" :
            		if ( !signature.contains("OUTPUT_TACHO:TACHO") )
            		{
            			ev3.getMqttHelper().debug("No LargeMotor on Port " +Ports[i]);
            			ids.close();
            			return false;
            		}
            		break;
            	case "ev3steuerung.rezeptabarbeitung.TouchSensor" :
            		if ( !signature.contains("EV3_ANALOG:EV3_TOUCH") )
            		{
            			ev3.getMqttHelper().debug("No ToucSensor on Port " +Ports[i]);
            			ids.close();
            			return false;
            		}
            		break;
            	case "ev3steuerung.rezeptabarbeitung.ColorSensor" :
            		if ( !signature.contains("UART:COL-REFLECT") )
            		{
            			ev3.getMqttHelper().debug("No ColorSensor on Port " +Ports[i]);
            			ids.close();
            			return false;
            		}
            		break;
            	}
            	ids.close();
            }
        }
    	ev3.getMqttHelper().debug("Devices are Ok");
    	return true;
    }
    
    /**
     * Registers all Devices that are used from the Recipe
     * 
     * @throws lejos.hardware.DeviceException - When a Device is unable to open its port
     * @see Device
     * */
    public void register() throws lejos.hardware.DeviceException, NullPointerException {
    	ev3.getMqttHelper().debug("Recipe: "+this.recipeID+" registering Devices");
        /* Ger�te registrieren */
        
        for (int i = 0; i<8 ; i++){
            if(devices[i] != null){
            	if(!devices[i].register()) {
	            	System.out.println("Failed to register a Device");
	            	ev3.getMqttHelper().debug("Failed to register a Device");
	                throw new NullPointerException("Failed Registering a Device");
	            }
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
        ev3.getMqttHelper().debug("Recipe: "+this.recipeID+" close Devices");
        /* Verbindungen zu Motoren/Sensoren trennen */
        if(devices != null){
	        for (int i = 0; i<8 ; i++){
	            if(devices[i] != null){
	            	devices[i].close();
	            }
        	}
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
        ev3.getMqttHelper().debug("Recipe: "+this.recipeID+" working the tasks");
        boolean ok = true;
        
        /* Solange Rezeptbefehle vorhanden sind*/
        while(!rezept.isEmpty() && ok) { 
            
            int time = 0;
            
            /* Spin starten */
            if(rezept.getFirst().getClass().toString().contains("Spin")) {
                Spin[] befehl = (Spin[]) rezept.getFirst();
      
                for (Spin s:befehl) {
                    ev3.getMqttHelper().debug("Working on Spin "+s.toString());
                    
                    MotorDevice md = (MotorDevice)devices[s.getDevice()];
                    BaseRegulatedMotor em = md.getEV3Motor();
                    em.setStallThreshold(10, 50);
                    em.resetTachoCount();
                    switch(s.getMode()){
                    case Spin_Till_Pressed:
                    	System.out.println("Press TouchSensor");
                        ev3.getMqttHelper().debug("Wait until its pressed");
                        md.forward(s.getSpeed());                       
                    	break;
                    case Spin_Till_Released:
                    	System.out.println("Release TouchSensor");
                        ev3.getMqttHelper().debug("Wait until its released");
                        md.forward(s.getSpeed());
                    	break;
                    case Turn_to_Angle:
                        ev3.getMqttHelper().debug("Turn to an angle");
                        md.rotate(true, s.getSpeed(), s.getAngle());
                    	break;
                    	default:
                    		ev3.getMqttHelper().debug("Mode nicht erkannt");
                    }
                }
                time=0;
                boolean[] loop=new boolean[befehl.length];
                for(int i=0;i<loop.length;i++) {
                	loop[i]=true;
                }
                boolean mloop=true;
                while(mloop) {
	                for (int i=0; i<befehl.length; i++){
	                	Spin s=befehl[i];
                		MotorDevice md = (MotorDevice)devices[s.getDevice()];
                        BaseRegulatedMotor em = md.getEV3Motor();
	                	switch(s.getMode()) {
	                	case Spin_Till_Pressed:
	                		TouchSensor touch = (TouchSensor)devices[s.getSensor()];
	                        if (!touch.isPressed()) {
	                            if (em.isStalled()) {
	                            	for(Spin s2:befehl) 
	                                	((MotorDevice)devices[s2.getDevice()]).getEV3Motor().stop();
	                        		throw new InterruptedException("stall");
	                        	}
	                        } else {
	                        	loop[i] = false;
	                        }                   
	                        break;
	                	
	                        
	                	case Spin_Till_Released:
	                        TouchSensor touch2 = (TouchSensor)devices[s.getSensor()];
	                        if (touch2.isPressed()) {
	                            if (em.isStalled()) {
	                            	for(Spin s2:befehl) 
	                                	((MotorDevice)devices[s2.getDevice()]).getEV3Motor().stop();
	                        		throw new InterruptedException("stall");
	                        	}
	                        } else {
	                        	loop[i] = false;
	                        }                   
	                        break;
	                		
	                		
	                	case Turn_to_Angle:
	                		double progress=((double)em.getTachoCount())/s.getAngle();
	                		boolean end=progress>0.9 && progress < 1.1;
	                		if(em.isStalled() && !end) {
                        		for(Spin s2:befehl) 
                                	((MotorDevice)devices[s2.getDevice()]).getEV3Motor().stop();
                        		throw new InterruptedException("stall");
                        	} 
	                		if(!em.isMoving()) {
	                        	loop[i] = false;
	                        }
	                        break;
	                		
	                		
	                		default:
	                			ev3.getMqttHelper().debug("Modus nicht erkannt");
	                	}
	                }
	                mloop=false;
	                for(boolean lo:loop)
	                	mloop=mloop||lo;
	                Delay.msDelay(10);
                }
            }
            /* Wartezeit starten */
            else if(rezept.getFirst().getClass().toString().contains("Wait")) {
                ev3.getMqttHelper().debug("Waiting operation");
                Wait[] befehl = (Wait[]) rezept.getFirst();
                TouchSensor touch;
                time = 0;
                
                
                switch(befehl[0].getMode()) {
                case Wait_Time:
                    long toWait = befehl[0].getMs();
                    ev3.getMqttHelper().debug("Wait certain time");
                    Delay.msDelay(toWait);
                    break;
                    
                case Wait_for_Release:
                    touch = (TouchSensor)devices[befehl[0].getSensor()];
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
                
                case Wait_for_Press:
                    touch = (TouchSensor)devices[befehl[0].getSensor()];
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
  }
