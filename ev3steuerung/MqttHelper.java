package ev3steuerung;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.LinkedList;

/* only 4 Eclipse
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
*/


public class MqttHelper implements MqttCallback, Runnable {
	private MqttAsyncClient mqtt;
    private boolean error=false;
    private boolean debugMode=false;
    private String serverURI;
    private String deviceID;
    private EV3_Brick mqttBrick;
	private LinkedList<String> failedMessages;
    private String ip;
    
    public MqttHelper(EV3_Brick mqttBrick, String deviceID, String serverURI, String ip) {
        this.deviceID 		= 	deviceID;
        this.mqttBrick 		= 	mqttBrick;
        this.serverURI		= 	serverURI;
		this.failedMessages = 	new LinkedList<String>();
        this.ip				=	ip;
        connect();
    }
    private synchronized boolean connect() {
        try {
            mqtt=new MqttAsyncClient(serverURI, MqttAsyncClient.generateClientId());
            mqtt.setCallback(this);
            IMqttToken conToken = mqtt.connect();
            conToken.waitForCompletion();
            IMqttToken subToken = mqtt.subscribe(deviceID,0);  //Qos 0?
            subToken.waitForCompletion();
            subToken=mqtt.subscribe("vwp/"+deviceID,0);
            subToken.waitForCompletion();
            error=false;
            while(failedMessages.size()>0&&publishToMES(failedMessages.remove())); //care: while has no body
            if(failedMessages.size()==0) return publishToDeviceID(mqttBrick.getState());
            return false;
        } catch (MqttException mqtte) {
            error=true;
            return false;
        }
    }
    private synchronized boolean publishToDeviceID(String message) {
        if(error&&!connect()) {
            return false;
        }
        try {
            mqtt.publish(deviceID, new MqttMessage(message.getBytes()));
            return true;
        } catch (MqttException e) {
            error=true;
            return false;
        }
    }
    private synchronized boolean publishToMES(String message) {
        try {
            mqtt.publish("vwp/stiserver", new MqttMessage(message.getBytes()));
        } catch (MqttException e) {
            failedMessages.add(message);
            new Thread(this).start();   // if we fail delivering a message to MES, start a thread to try to fix the situation and deliver the message
        }
        return false;
    }
    public synchronized boolean register() {
        return publishToMES(deviceID+":register:{\"ip\":\""+ip+"\",\"name\":\""+deviceID+"\",\"status\":\""+mqttBrick.getState()+"\"}");
    }
    public synchronized boolean requestTask() {
        return publishToMES(deviceID+":TaskREQ");
    }
    public synchronized boolean indicateTask(String task, String result) {
        return publishToMES(deviceID+":TaskIND:"+task+":"+result);
    }
    public synchronized boolean indicateState(String message) {
        return publishToMES(deviceID+":StateIND:"+message);
    }
    public synchronized void publishState() {
        publishToDeviceID(mqttBrick.getState());
    }
    public synchronized void debug(String message) {
        if(debugMode) publishToDeviceID("debug "+message);
    }
    @Override
    public synchronized void connectionLost(Throwable cause) {
        error=true;
        // no need to inform anybody, will try to fix when next message shall be sent
    }
    @Override
    public synchronized void deliveryComplete(IMqttDeliveryToken token) {
        //nothing yeah
    }
    @Override
    public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
        if(("vwp/"+deviceID).equals(topic)) {
            mqttBrick.messageArrived(new String(message.getPayload()));
        } else {
            String[] information=new String(message.getPayload()).split(" ");
            switch(information.length) {
            case 1:
                switch(information[0]) {
                case "hello":
                    publishToDeviceID(mqttBrick.getState());
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
                    else System.out.println("unrecognized message "+new String(message.getPayload()));
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
    }
    private synchronized void fix() {
        if(error) connect();
    }
    public synchronized void close() {
        try {
            publishToMES(deviceID+"shutting down");
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
    public void run() {
        try {
            while(error) {
                fix();
                Thread.sleep(10000);
            } 
        } catch (InterruptedException e) {
        }
    }
}