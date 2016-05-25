package ev3steuerung;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttHelper implements MqttCallback {
    private MqttAsyncClient mqtt;
    private boolean error=false;
    private boolean debugMode=false;
    private String serverURI;
    private String deviceID;
    private MqttBrick mqttBrick;
    public MqttHelper(MqttBrick mqttBrick, String deviceID, String serverURI) {
        this.deviceID=deviceID;
        this.mqttBrick=mqttBrick;
        this.serverURI=serverURI;
        connect();
    }
    private boolean connect() {
        try {
            mqtt=new MqttAsyncClient(serverURI, MqttAsyncClient.generateClientId());
            mqtt.setCallback(this);
            IMqttToken conToken = mqtt.connect();
            conToken.waitForCompletion();
            IMqttToken subToken = mqtt.subscribe(deviceID,0);  //Qos 0?
            subToken.waitForCompletion();
            error=false;
            //TODO: model.mqttConnected();
            return publish(mqttBrick.getState());
        } catch (MqttException mqtte) {
            //TODO: model.mqttConnectionLost();
            error=true;
            return false;
        }
    }
    private synchronized boolean publish(String message) {
        if(error&&!connect()) {
            return false;
        }
        try {
            mqtt.publish(deviceID, new MqttMessage(message.getBytes()));
            return true;
        } catch (MqttException e) {
            //TODO:  model.mqttConnectionLost();
            error=true;
            return false;
        }
    }
    public synchronized void publishState() {
        publishState();
    }
    public synchronized void debug(String message) {
        if(debugMode) publish("debug "+message);
    }
    @Override
    public synchronized void connectionLost(Throwable cause) {
        error=true;
        //TODO:  model.mqttConnectionLost();
    }
    @Override
    public synchronized void deliveryComplete(IMqttDeliveryToken token) {
        //TODO
    }
    @Override
    public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
        String[] information=new String(message.getPayload()).split(" ");
        switch(information.length) {
        case 1:
            switch(information[0]) {
            case "hello":
                publish(mqttBrick.getState());
                break;
            case "IDLE":
            case "PROC":
            case "MAINT":
            case "DOWN":
                break;
            default:
                System.out.println("unrecognized message "+new String(message.getPayload()));
            }
            break;
        case 2:
            switch(information[0]) {
            case "debug":
                if("true".equals(information[1])||"false".equals(information[1])) debugMode=Boolean.parseBoolean(information[1]);
                break;
            case "manual":
                mqttBrick.manualFix();
                break;
            case "emergency":
                mqttBrick.emergencyShutdown();
                break;
            default:
                System.out.println("unrecognized message "+new String(message.getPayload()));
            }
            break;
        default:
            System.out.println("unrecognized message "+new String(message.getPayload()));
        }
    }
    public void fix() {
        if(error) error=!connect();
    }
    public void close() {
        try {
            mqtt.disconnect();
            mqtt.close();
        } catch (MqttException e) {
            //TODO
        }
        String[] directories=Paths.get("").toAbsolutePath().toFile().list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith("paho")) return true;
                return false;
            } 
        });
        for(String directory:directories) {
            debug("removing directory "+directory);
            new File(directory+"/.lck").delete();
            new File(directory).delete();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        MqttHelper mqttHelper=new MqttHelper(new MqttBrick(){
            @Override
            public String getState() {
                return "IDLE";
            }
            @Override
            public void manualFix() {
                System.out.println("manual fix");
            }

            @Override
            public void emergencyShutdown() {
                System.out.println("emergency shutdown");
            }
        },"STP1001", "tcp://localhost");
        Thread.sleep(1000000);
        mqttHelper.close();
    }
}
