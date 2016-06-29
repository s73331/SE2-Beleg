package ev3steuerung;

/**
 * Control is the Main-Class of the EV3-Steuerung.
 * Here it begins and it ends.
 * 
 * @author Christoph Schmidt
 * @version 0.8
 * @since 01.04.2016 */
 
public class Control {
    private boolean running;
    
    private Control () {
       running = true;
    }
    
    private void start() {
        EV3_Brick ev3 = EV3_Brick.getInstance();
        ev3.getMqttHelper().debug("STRG: Start of the Machine");
        while (running) {
            if (ev3.getStateName().equals("DOWN"))
                running = false;
            ev3.getState().doAction();
        }
        ev3.getMqttHelper().debug("STRG: End of the Machine");
        ev3.stopMqtt();
        fixDir();
    }
    
    private boolean checkExtClasses() {
        boolean ok = true;
        String[] classes = {"lejos.utility.Delay", "lejos.hardware.Audio", "org.eclipse.paho.client.mqttv3.MqttMessage"};
        
        for (String c : classes) {
            try {
                Class.forName(c);
            } catch( ClassNotFoundException e ) {
                System.out.println("Class "+c+" not found!");
                ok = false;
            }
        }
        
        return ok;
    }
    
    private void fixDir() {
        String[] directories = java.nio.file.Paths.get("").toAbsolutePath().toFile().list(new java.io.FilenameFilter(){
            @Override
            public boolean accept(java.io.File dir, String name) {
                if(name.startsWith("paho")) return true;
                return false;
            } 
        });
        for(String directory:directories) {
            new java.io.File(directory+"/.lck").delete();
            new java.io.File(directory).delete();
        }
    }
    
    /**
     * Starts the EV3-Steuerung without need of Arguments
     * 
     * @see EV3_Brick
     * @param args - Arguments of the MAIN (not needed) */
    public static void main(String[] args) {
        Control m = new Control();
        if (m.checkExtClasses())
            m.start();
        else
            System.out.println("Not all Classes could be loaded. Exiting..");
        System.exit(1);
    }
}
